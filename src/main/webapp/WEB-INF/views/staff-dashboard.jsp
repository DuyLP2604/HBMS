<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Staff Dashboard" useBootstrap="true"
               pageCss="staff-dashboard.css" bodyClass="staff-dashboard-page">
    <c:choose>
        <c:when test="${isReceptionist}">
            <c:set var="desk" value="${receptionDashboard}" />
            <c:url var="deskHome" value="/staff/dashboard" />
            <c:url var="deskArrivals" value="/staff/dashboard"><c:param name="focus" value="arrivals" /></c:url>
            <c:url var="deskDepartures" value="/staff/dashboard"><c:param name="focus" value="departures" /></c:url>
            <c:url var="deskAssignments" value="/staff/dashboard"><c:param name="focus" value="assignments" /></c:url>
            <c:url var="deskReview" value="/staff/dashboard"><c:param name="focus" value="review" /></c:url>
            <c:url var="deskRefresh" value="/staff/dashboard"><c:param name="focus" value="${desk.focus}" /></c:url>
            <div class="reception-dashboard">
                <header class="reception-header">
                    <div>
                        <p class="reception-eyebrow"><span class="reception-brand-mark" aria-hidden="true"></span> FRONT DESK</p>
                        <h1>Front desk overview</h1>
                        <p class="reception-welcome">Hello, <strong><c:out value="${employee.fullName}" /></strong>. Here is what needs your attention today.</p>
                    </div>
                    <div class="reception-header-actions">
                        <a class="reception-button reception-button-primary" href="${pageContext.request.contextPath}/staff/counter-booking">
                            <i class="fa-solid fa-plus" aria-hidden="true"></i> Book for customer
                        </a>
                        <a class="reception-profile-link" href="${pageContext.request.contextPath}/staff/profile">My staff profile <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                    </div>
                </header>
                <div class="reception-datebar">
                    <p><i class="fa-regular fa-calendar" aria-hidden="true"></i> <c:out value="${dashboardDate}" /><span class="reception-date-zone">Vietnam time</span></p>
                    <div class="reception-updated">
                        <c:if test="${not dashboardUnavailable}"><span>Updated <c:out value="${dashboardTime}" /></span></c:if>
                        <a href="<c:out value='${deskRefresh}' />" class="reception-refresh" aria-label="Refresh today's overview"><i class="fa-solid fa-rotate-right" aria-hidden="true"></i> Refresh</a>
                    </div>
                </div>
                <c:if test="${not empty successMessage}"><div class="alert alert-success" role="status"><c:out value="${successMessage}" /></div></c:if>
                <c:if test="${not empty errorMessage}"><div class="alert alert-danger" role="alert"><c:out value="${errorMessage}" /></div></c:if>
                <c:choose>
                    <c:when test="${dashboardUnavailable}">
                        <div class="reception-unavailable" role="alert">
                            <i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i>
                            <div><h2>Today's overview is temporarily unavailable</h2><p>Refresh to try again, or use the shortcuts below to continue working.</p></div>
                            <a href="<c:out value='${deskHome}' />" class="reception-button reception-button-secondary">Try again</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <section class="reception-metrics" aria-label="Outstanding front-desk work">
                            <a class="reception-metric ${desk.focus eq 'arrivals' ? 'is-active' : ''}" href="<c:out value='${deskArrivals}' />#today-priorities">
                                <span class="reception-metric-top">Expected arrivals <i class="fa-solid fa-arrow-right-to-bracket" aria-hidden="true"></i></span>
                                <strong><c:out value="${desk.arrivals}" /></strong><span class="reception-metric-note">Due today, not yet checked in</span>
                            </a>
                            <a class="reception-metric ${desk.focus eq 'departures' ? 'is-active' : ''}" href="<c:out value='${deskDepartures}' />#today-priorities">
                                <span class="reception-metric-top">Departures due <i class="fa-solid fa-arrow-right-from-bracket" aria-hidden="true"></i></span>
                                <strong><c:out value="${desk.departures}" /></strong><span class="reception-metric-note">Today and overdue departures</span>
                            </a>
                            <a class="reception-metric ${desk.focus eq 'assignments' ? 'is-active' : ''}" href="<c:out value='${deskAssignments}' />#today-priorities">
                                <span class="reception-metric-top">Awaiting rooms <i class="fa-solid fa-key" aria-hidden="true"></i></span>
                                <strong><c:out value="${desk.assignments}" /></strong><span class="reception-metric-note"><c:out value="${desk.assignmentsToday}" /> to assign today · <c:out value="${desk.assignmentsUpcoming}" /> upcoming</span>
                            </a>
                            <a class="reception-metric reception-metric-review ${desk.reviews gt 0 ? 'has-attention' : ''} ${desk.focus eq 'review' ? 'is-active' : ''}" href="<c:out value='${deskReview}' />#today-priorities">
                                <span class="reception-metric-top">Needs review <i class="fa-regular fa-flag" aria-hidden="true"></i></span>
                                <strong><c:out value="${desk.reviews}" /></strong><span class="reception-metric-note">Overdue or unresolved stays</span>
                            </a>
                        </section>
                        <c:if test="${desk.reviews gt 0}">
                            <aside class="reception-attention" aria-label="Priority reminder">
                                <span class="reception-attention-icon"><i class="fa-solid fa-flag" aria-hidden="true"></i></span>
                                <div><strong>Review overdue stays first.</strong><p><c:out value="${desk.reviews}" /> <c:choose><c:when test="${desk.reviews eq 1}">booking is</c:when><c:otherwise>bookings are</c:otherwise></c:choose> overdue or unresolved. Review these first.</p></div>
                                <a href="<c:out value='${deskReview}' />#today-priorities">Review now <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                            </aside>
                        </c:if>
                    </c:otherwise>
                </c:choose>
                <div class="reception-workspace">
                    <section class="reception-panel reception-priorities" id="today-priorities" aria-labelledby="reception-priorities-title">
                        <div class="reception-panel-heading">
                            <div><p class="reception-section-kicker">YOUR WORKLIST</p><h2 id="reception-priorities-title"><c:choose><c:when test="${desk.focus eq 'assignments'}">Room assignment queue</c:when><c:otherwise>Today's priorities</c:otherwise></c:choose> <c:if test="${not dashboardUnavailable}"><span class="reception-count"><c:out value="${desk.focus eq 'assignments' ? desk.assignments : desk.total}" /></span></c:if></h2><p><c:choose><c:when test="${desk.focus eq 'assignments'}">Bookings awaiting rooms for today and upcoming stays.</c:when><c:otherwise>Outstanding work for today, with overdue stays first.</c:otherwise></c:choose></p></div>
                            <a href="${pageContext.request.contextPath}/staff/stays" class="reception-text-link">Guest stays <i class="fa-solid fa-arrow-up-right-from-square" aria-hidden="true"></i></a>
                        </div>
                        <c:if test="${not dashboardUnavailable}">
                            <nav class="reception-filters" aria-label="Filter reception worklist">
                                <a href="<c:out value='${deskHome}' />#today-priorities" class="${desk.focus eq 'all' ? 'is-active' : ''}" ${desk.focus eq 'all' ? 'aria-current="page"' : ''}>All priorities</a>
                                <a href="<c:out value='${deskArrivals}' />#today-priorities" class="${desk.focus eq 'arrivals' ? 'is-active' : ''}" ${desk.focus eq 'arrivals' ? 'aria-current="page"' : ''}>Arrivals</a>
                                <a href="<c:out value='${deskDepartures}' />#today-priorities" class="${desk.focus eq 'departures' ? 'is-active' : ''}" ${desk.focus eq 'departures' ? 'aria-current="page"' : ''}>Departures</a>
                                <a href="<c:out value='${deskAssignments}' />#today-priorities" class="${desk.focus eq 'assignments' ? 'is-active' : ''}" ${desk.focus eq 'assignments' ? 'aria-current="page"' : ''}>Room assignment</a>
                                <a href="<c:out value='${deskReview}' />#today-priorities" class="${desk.focus eq 'review' ? 'is-active' : ''}" ${desk.focus eq 'review' ? 'aria-current="page"' : ''}>Needs review</a>
                            </nav>
                        </c:if>
                        <c:choose>
                            <c:when test="${dashboardUnavailable}">
                                <div class="reception-empty"><span class="reception-empty-icon"><i class="fa-solid fa-cloud" aria-hidden="true"></i></span><h3>Worklist could not be loaded</h3><p>Your bookings have not been changed. Please refresh to load the latest worklist.</p></div>
                            </c:when>
                            <c:when test="${empty desk.tasks}">
                                <div class="reception-empty"><span class="reception-empty-icon"><i class="fa-solid fa-check" aria-hidden="true"></i></span><h3><c:choose><c:when test="${desk.focus eq 'all'}">You're up to date.</c:when><c:otherwise>No pending items in this view.</c:otherwise></c:choose></h3><p><c:choose><c:when test="${desk.focus eq 'all'}">No outstanding arrivals, room assignments or departures for today.</c:when><c:otherwise>Choose another view to see the rest of today's work.</c:otherwise></c:choose></p><c:if test="${desk.focus ne 'all'}"><a class="reception-text-link" href="<c:out value='${deskHome}' />#today-priorities">Show all priorities <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></c:if></div>
                            </c:when>
                            <c:otherwise>
                                <table class="reception-table">
                                    <caption class="reception-sr-only">Outstanding arrivals, room assignments and departures. Overdue stays appear first.</caption>
                                    <thead><tr><th scope="col">Guest</th><th scope="col">Next step</th><th scope="col">Due</th><th scope="col"><span class="reception-sr-only">Action</span></th></tr></thead>
                                    <tbody>
                                        <c:forEach var="task" items="${desk.tasks}">
                                            <c:url var="taskUrl" value="${task.actionPath}">
                                                <c:if test="${task.actionPath ne '/staff/stays'}"><c:param name="bookingID" value="${task.bookingID}" /></c:if>
                                                <c:if test="${not empty task.action}"><c:param name="action" value="${task.action}" /></c:if>
                                            </c:url>
                                            <tr>
                                                <td class="reception-guest-cell"><div class="reception-guest-name"><c:out value="${empty task.guestName ? 'Guest' : task.guestName}" /></div><div class="reception-guest-meta"><c:out value="${task.bookingID}" /><c:if test="${not empty task.phone}"><span aria-hidden="true"> · </span><c:out value="${task.phone}" /></c:if></div></td>
                                                <td class="reception-status-cell"><span class="reception-status reception-status-${task.tone}"><span aria-hidden="true"></span><c:out value="${task.label}" /></span></td>
                                                <td class="reception-due-cell"><c:out value="${task.dueLabel}" /></td>
                                                <td class="reception-action-cell"><a class="reception-row-action" href="<c:out value='${taskUrl}' />" aria-label="<c:out value='${task.actionLabel}' /> for booking <c:out value='${task.bookingID}' />"><c:out value="${task.actionLabel}" /><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                                <c:url var="remainingStaysUrl" value="/staff/stays"><c:if test="${desk.focus eq 'assignments'}"><c:param name="view" value="assignments" /></c:if></c:url>
                                <div class="reception-table-footer"><span>Showing <c:out value="${desk.tasks.size()}" /> of <c:out value="${desk.filteredTotal}" /> pending bookings</span><c:if test="${desk.filteredTotal gt desk.tasks.size()}"><a href="${remainingStaysUrl}">${desk.focus eq 'assignments' ? 'View all awaiting rooms' : 'Open guest stays'} <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></c:if></div>
                            </c:otherwise>
                        </c:choose>
                    </section>
                    <aside class="reception-panel reception-shortcuts" aria-labelledby="reception-shortcuts-title">
                        <div class="reception-panel-heading"><div><p class="reception-section-kicker">AT YOUR FINGERTIPS</p><h2 id="reception-shortcuts-title">Front-desk tools</h2></div></div>
                        <nav aria-label="Front-desk tools">
                            <a href="${pageContext.request.contextPath}/staff/stays"><span class="reception-tool-icon"><i class="fa-solid fa-calendar-check" aria-hidden="true"></i></span><span><strong>Guest stays</strong><small>Arrivals and stay status</small></span><i class="fa-solid fa-chevron-right" aria-hidden="true"></i></a>
                            <a href="${pageContext.request.contextPath}/staff/bookings"><span class="reception-tool-icon"><i class="fa-solid fa-key" aria-hidden="true"></i></span><span><strong>Room assignment</strong><small>Match bookings with rooms</small></span><i class="fa-solid fa-chevron-right" aria-hidden="true"></i></a>
                            <a href="${pageContext.request.contextPath}/CheckoutEmployee"><span class="reception-tool-icon"><i class="fa-solid fa-arrow-right-from-bracket" aria-hidden="true"></i></span><span><strong>Check-out</strong><small>Review bills and departures</small></span><i class="fa-solid fa-chevron-right" aria-hidden="true"></i></a>
                            <a href="${pageContext.request.contextPath}/checkin"><span class="reception-tool-icon"><i class="fa-solid fa-door-open" aria-hidden="true"></i></span><span><strong>In-house guests</strong><small>Guests currently staying</small></span><i class="fa-solid fa-chevron-right" aria-hidden="true"></i></a>
                            <a href="${pageContext.request.contextPath}/customer?action=list"><span class="reception-tool-icon"><i class="fa-solid fa-user-group" aria-hidden="true"></i></span><span><strong>Customers</strong><small>Guest profiles and contacts</small></span><i class="fa-solid fa-chevron-right" aria-hidden="true"></i></a>
                            <a href="${pageContext.request.contextPath}/invoice"><span class="reception-tool-icon"><i class="fa-solid fa-file-invoice" aria-hidden="true"></i></span><span><strong>Invoices</strong><small>Find and review invoices</small></span><i class="fa-solid fa-chevron-right" aria-hidden="true"></i></a>
                        </nav>
                        <div class="reception-room-link"><i class="fa-solid fa-bed" aria-hidden="true"></i><div><strong>Looking for a room?</strong><a href="${pageContext.request.contextPath}/room">Browse hotel rooms <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></div></div>
                    </aside>
                </div>
                <footer class="reception-footnote"><span><i class="fa-solid fa-shield-halved" aria-hidden="true"></i> Simple Bear Hotel · Reception workspace</span><span>Counts show bookings awaiting action.</span></footer>
            </div>
        </c:when>
        <c:otherwise>
            <c:set var="staffPosition" value="${fn:toLowerCase(fn:trim(employee.position))}" />
            <c:set var="serviceStaff" value="${staffPosition eq 'service staff'}" />
            <c:set var="careStaff" value="${staffPosition eq 'customer service staff'}" />
            <c:url var="staffProfileUrl" value="/staff/profile" />
            <c:url var="serviceOrdersUrl" value="/service"><c:param name="action" value="orders" /></c:url>
            <c:url var="serviceCatalogUrl" value="/service"><c:param name="action" value="manage" /></c:url>
            <c:url var="complaintsUrl" value="/complaint"><c:param name="action" value="list" /></c:url>
            <c:url var="customersUrl" value="/customer"><c:param name="action" value="list" /></c:url>

            <div class="operations-dashboard">
                <header class="operations-header">
                    <div>
                        <p class="operations-eyebrow"><span aria-hidden="true"></span><c:choose><c:when test="${serviceStaff}">SERVICE OPERATIONS</c:when><c:when test="${careStaff}">GUEST RELATIONS</c:when><c:otherwise>HOTEL OPERATIONS</c:otherwise></c:choose></p>
                        <h1><c:choose><c:when test="${serviceStaff}">Service overview</c:when><c:when test="${careStaff}">Guest care overview</c:when><c:otherwise>Staff overview</c:otherwise></c:choose></h1>
                        <p class="operations-welcome">Hello, <strong><c:out value="${empty fn:trim(employee.fullName) ? sessionScope.user.username : employee.fullName}" /></strong>. <c:choose><c:when test="${serviceStaff}">Your tools for taking care of guest requests.</c:when><c:when test="${careStaff}">Your tools for helping guests and following up on feedback.</c:when><c:otherwise>Your staff workspace at Simple Bear Hotel.</c:otherwise></c:choose></p>
                    </div>
                    <a href="${staffProfileUrl}" class="operations-profile-link"><i class="fa-regular fa-user" aria-hidden="true"></i> My staff profile <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                </header>

                <c:if test="${not empty successMessage}"><div class="alert alert-success" role="status"><c:out value="${successMessage}" /></div></c:if>
                <c:if test="${not empty errorMessage}"><div class="alert alert-danger" role="alert"><c:out value="${errorMessage}" /></div></c:if>

                <c:choose>
                    <c:when test="${serviceStaff or careStaff}">
                        <div class="operations-workspace">
                            <div class="operations-main">
                                <section class="operations-focus" aria-labelledby="operations-focus-title">
                                    <div>
                                        <p class="operations-kicker">START HERE</p>
                                        <h2 id="operations-focus-title"><c:choose><c:when test="${serviceStaff}">Guest requests come first.</c:when><c:otherwise>Start with guest feedback.</c:otherwise></c:choose></h2>
                                        <p class="operations-focus-description"><c:choose><c:when test="${serviceStaff}">Review the services requested by in-house guests before coordinating delivery.</c:when><c:otherwise>Review guest complaints, follow up on their concerns and keep the response status up to date.</c:otherwise></c:choose></p>
                                    </div>
                                    <a href="${serviceStaff ? serviceOrdersUrl : complaintsUrl}" class="operations-button operations-button-gold"><c:choose><c:when test="${serviceStaff}">View service orders</c:when><c:otherwise>Review complaints</c:otherwise></c:choose><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                                </section>

                                <section class="operations-tools" aria-labelledby="operations-tools-title">
                                    <div class="operations-section-heading"><h2 id="operations-tools-title">Daily tools</h2><span><c:choose><c:when test="${serviceStaff}">Guest services</c:when><c:otherwise>Customer support</c:otherwise></c:choose></span></div>
                                    <div class="operations-tool-grid">
                                        <c:choose>
                                            <c:when test="${serviceStaff}">
                                                <article class="operations-tool-card">
                                                    <div class="operations-tool-heading"><span class="operations-tool-icon" aria-hidden="true"><i class="fa-solid fa-clipboard-list"></i></span><h3>Booked services</h3></div>
                                                    <p>View guest requests, booking details, quantities and service charges.</p>
                                                    <a href="${serviceOrdersUrl}" class="operations-tool-link">View booked services <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                                                </article>
                                                <article class="operations-tool-card">
                                                    <div class="operations-tool-heading"><span class="operations-tool-icon operations-tool-icon-warm" aria-hidden="true"><i class="fa-solid fa-bell-concierge"></i></span><h3>Service catalog</h3></div>
                                                    <p>Find available hotel services and check their current prices.</p>
                                                    <a href="${serviceCatalogUrl}" class="operations-tool-link">Browse service catalog <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                                                </article>
                                            </c:when>
                                            <c:otherwise>
                                                <article class="operations-tool-card">
                                                    <div class="operations-tool-heading"><span class="operations-tool-icon" aria-hidden="true"><i class="fa-regular fa-comments"></i></span><h3>Complaints &amp; replies</h3></div>
                                                    <p>Read guest feedback, send a reply and update the resolution status.</p>
                                                    <a href="${complaintsUrl}" class="operations-tool-link">Manage complaints <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                                                </article>
                                                <article class="operations-tool-card">
                                                    <div class="operations-tool-heading"><span class="operations-tool-icon operations-tool-icon-warm" aria-hidden="true"><i class="fa-solid fa-user-group"></i></span><h3>Customer profiles</h3></div>
                                                    <p>Find a guest's profile and contact details when you need more context.</p>
                                                    <a href="${customersUrl}" class="operations-tool-link">Find a customer <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                                                </article>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </section>
                            </div>

                            <aside class="operations-guide" aria-labelledby="operations-guide-title">
                                <div class="operations-guide-heading"><p class="operations-kicker">YOUR WORKFLOW</p><h2 id="operations-guide-title">A clear next step</h2></div>
                                <ol class="operations-steps">
                                    <c:choose>
                                        <c:when test="${serviceStaff}">
                                            <li><span aria-hidden="true">1</span><div><h3>Review guest requests</h3><p>Check the booking, guest and services requested.</p></div></li>
                                            <li><span aria-hidden="true">2</span><div><h3>Confirm the details</h3><p>Review quantities and check the catalog for current prices.</p></div></li>
                                            <li><span aria-hidden="true">3</span><div><h3>Coordinate delivery</h3><p>Use the booking details to arrange the requested service.</p></div></li>
                                        </c:when>
                                        <c:otherwise>
                                            <li><span aria-hidden="true">1</span><div><h3>Understand the concern</h3><p>Read the guest's message and check its current status.</p></div></li>
                                            <li><span aria-hidden="true">2</span><div><h3>Check guest details</h3><p>Look up the customer profile when more context is needed.</p></div></li>
                                            <li><span aria-hidden="true">3</span><div><h3>Reply and follow up</h3><p>Send a clear response and update the complaint status.</p></div></li>
                                        </c:otherwise>
                                    </c:choose>
                                </ol>
                                <div class="operations-shift"><i class="fa-regular fa-clock" aria-hidden="true"></i><div><span>Your shift</span><strong><c:out value="${empty fn:trim(employee.shift) ? 'Not assigned' : employee.shift}" /></strong></div></div>
                            </aside>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <section class="operations-unassigned" aria-labelledby="operations-unassigned-title"><span class="operations-tool-icon" aria-hidden="true"><i class="fa-solid fa-id-badge"></i></span><h2 id="operations-unassigned-title">Your staff account</h2><p>Contact your administrator to confirm the tools assigned to your position.</p><a href="${staffProfileUrl}" class="operations-button operations-button-green">View my staff profile <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></section>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:otherwise>
    </c:choose>
</layout:layout>
