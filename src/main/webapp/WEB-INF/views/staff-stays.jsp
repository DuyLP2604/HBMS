<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Shows guest stays and the actions reception can take.
--%>
<layout:layout title="Guest Stays" pageCss="stay-workspace.css" pageJs="stay-list.js" bodyClass="stay-workspace-page">
    <layout:stayList bookings="${bookings}" noShowBookings="${noShowBookings}" waitingMode="false" />
</layout:layout>
