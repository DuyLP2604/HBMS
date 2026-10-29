<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Room Assignment" pageCss="stay-workspace.css" pageJs="stay-list.js" bodyClass="stay-workspace-page">
    <layout:stayList bookings="${bookings}" noShowBookings="${noShowBookings}" waitingMode="true" />
</layout:layout>
