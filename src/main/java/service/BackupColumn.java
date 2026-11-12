package service;

/** Database column metadata used to read and restore workbook rows. */
final class BackupColumn {
    final String name;
    final int type;
    final boolean key;
    final boolean identity;

    BackupColumn(String name, int type, boolean key, boolean identity) {
        this.name = name;
        this.type = type;
        this.key = key;
        this.identity = identity;
    }
}
