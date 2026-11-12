"""Kiểm tra SELECT của view thật trên dữ liệu giả lập, không cần thư viện ngoài.

SQLite chỉ được dùng để kiểm tra quan hệ và bộ lọc của SELECT chuẩn SQL.
Migration, stored procedure và transaction T-SQL cần kiểm tra riêng trên SQL Server.
"""

from pathlib import Path
import re
import sqlite3
import unittest


ROOT = Path(__file__).resolve().parents[3]


class InvoicePaymentViewTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(":memory:")
        self.db.row_factory = sqlite3.Row
        self.addCleanup(self.db.close)
        self.db.executescript('''
            CREATE TABLE INVOICE (InvoiceID TEXT PRIMARY KEY, InvoiceDate TEXT,
                BookingID TEXT UNIQUE, TotalAmount NUMERIC);
            CREATE TABLE PAYMENT (PaymentID TEXT PRIMARY KEY, BookingID TEXT,
                TransactionID TEXT, PaymentType TEXT, PaymentTime TEXT, Status TEXT,
                MethodID TEXT, WalletAmount NUMERIC, ExternalAmount NUMERIC);
            CREATE TABLE [TRANSACTION] (id INTEGER PRIMARY KEY, TransactionID TEXT UNIQUE,
                TransactionType TEXT, TransactionValue NUMERIC, TransactionCode TEXT);
            CREATE TABLE PAYMENTMETHOD (MethodID TEXT PRIMARY KEY, MethodName TEXT);
            INSERT INTO PAYMENTMETHOD VALUES ('PT03', 'Cash'), ('PT01', 'VietQR'), ('PT02', 'Internal wallet');
            INSERT INTO INVOICE VALUES ('HD0001', '2026-11-12', 'BK0001', 1000);
            INSERT INTO INVOICE VALUES ('HD0002', '2026-11-12', 'BK0002', 2000);
        ''')
        initializer = (ROOT / "db/DB_v6_merged_transaction.sql").read_text(encoding="utf-8-sig")
        view = re.search(r"CREATE VIEW V_INVOICE_TRANSACTION_DETAILS AS\n(.*?);\nGO", initializer, re.S)
        self.assertIsNotNone(view)
        # Chỉ bỏ định danh schema SQL Server; giữ nguyên SELECT thực tế của project.
        self.db.execute("CREATE VIEW V_INVOICE_TRANSACTION_DETAILS AS " + view[1].replace("dbo.", ""))

    def payment(self, payment_id, booking="BK0001", status="PAID", amount=300,
                payment_type="DEPOSIT", method="PT03", wallet=0, ledger=True):
        transaction_id = "TX-" + payment_id
        if ledger:
            self.db.execute('INSERT INTO [TRANSACTION] (TransactionID, TransactionType, TransactionValue, TransactionCode) '
                            'VALUES (?, ?, ?, ?)', (transaction_id, "PAYMENT", amount, "REF-" + payment_id))
        self.db.execute('INSERT INTO PAYMENT VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
                        (payment_id, booking, transaction_id, payment_type, "2026-11-12 09:00:00",
                         status, method, wallet, amount - wallet))

    def rows(self, invoice="HD0001"):
        return self.db.execute("SELECT * FROM V_INVOICE_TRANSACTION_DETAILS WHERE InvoiceID = ? ORDER BY PaymentID",
                               (invoice,)).fetchall()

    def test_deposit_and_balance_appear_once(self):
        self.payment("PM0001")
        self.payment("PM0002", amount=700, payment_type="BALANCE")
        rows = self.rows()
        self.assertEqual(["PM0001", "PM0002"], [r["PaymentID"] for r in rows])
        self.assertEqual(1000, sum(r["TransactionValue"] for r in rows))

    def test_booking_payments_do_not_leak_between_invoices(self):
        self.payment("PM0001")
        self.payment("PM0002", booking="BK0002")
        self.assertEqual(["PM0001"], [r["PaymentID"] for r in self.rows()])
        self.assertEqual(["PM0002"], [r["PaymentID"] for r in self.rows("HD0002")])

    def test_pending_and_failed_are_excluded(self):
        self.payment("PM0001", status="PENDING")
        self.payment("PM0002", status="FAILED")
        self.assertEqual([], self.rows())

    def test_refunded_capture_is_kept_for_history(self):
        self.payment("PM0001", status="REFUNDED")
        self.assertEqual("REFUNDED", self.rows()[0]["PaymentStatus"])

    def test_booking_without_invoice_does_not_create_details(self):
        self.payment("PM0001", booking="BK0003")
        self.assertEqual(0, self.db.execute("SELECT COUNT(*) FROM V_INVOICE_TRANSACTION_DETAILS").fetchone()[0])

    def test_missing_transaction_does_not_attach_another_ledger_entry(self):
        self.payment("PM0001", ledger=False)
        self.payment("PM0002", booking="BK0002")
        self.assertEqual([], self.rows())

    def test_wallet_split_and_existing_columns_are_preserved(self):
        self.payment("PM0001", amount=1000, method="PT01", wallet=300)
        row = self.rows()[0]
        self.assertEqual(17, len(row.keys()))
        self.assertEqual((1000, 300, 700, "VietQR", "REF-PM0001"),
                         (row["InvoiceAmount"], row["WalletAmount"], row["ExternalAmount"],
                          row["MethodName"], row["TransactionCode"]))
        self.assertEqual("TX-PM0001", row["TransactionID"])


if __name__ == "__main__":
    unittest.main(verbosity=2)
