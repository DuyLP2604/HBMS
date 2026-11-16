<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Shows guest stays and the actions reception can take.
--%>
<layout:layout title="Guest Stays" pageCss="stay-workspace.css" pageJs="stay-list.js" bodyClass="stay-workspace-page">
    <layout:stayList bookings="${bookings}" noShowBookings="${noShowBookings}" waitingMode="false" />
    <script>
        (() => {
            const list = document.querySelector('[data-stay-list][data-mode="stays"]');
            if (!list) return;

            const dateFormatter = new Intl.DateTimeFormat('en-GB', {
                timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit'
            });
            const vietnamDate = () => {
                const parts = Object.fromEntries(dateFormatter.formatToParts(new Date())
                    .map(part => [part.type, part.value]));
                return [parts.year, parts.month, parts.day].join('-');
            };

            const today = vietnamDate();
            // Filter before stay-list.js reads the rows for search and counts.
            list.querySelectorAll('[data-stay-row]').forEach(row => {
                if (row.dataset.arrival !== today) row.remove();
            });

            function refreshOnNewDay() {
                if (vietnamDate() !== today) window.location.reload();
            }

            window.addEventListener('pageshow', refreshOnNewDay);
            document.addEventListener('visibilitychange', () => {
                if (!document.hidden) refreshOnNewDay();
            });
            window.setInterval(refreshOnNewDay, 60000);
        })();
    </script>
</layout:layout>