(() => {
    'use strict';
    const root = document.querySelector('[data-stay-list]');
    if (!root) return;
    const rows = Array.from(root.querySelectorAll('[data-stay-row]'));
    const buttons = Array.from(root.querySelectorAll('[data-stay-filter]'));
    const search = root.querySelector('[data-list-search]');
    const table = root.querySelector('[data-list-table]');
    const empty = root.querySelector('[data-list-empty]');
    const result = root.querySelector('[data-list-result]');
    const reset = root.querySelector('[data-reset-list]');
    const views = new Set(buttons.map(button => button.dataset.stayFilter));
    const dayFormatter = new Intl.DateTimeFormat('en-GB', {timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit'});
    const fold = value => String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[đĐ]/g, 'd').toLowerCase().replace(/\s+/g, ' ').trim();
    let currentDay = '';
    let view = 'all';
    const parameters = new URL(window.location.href).searchParams;
    if (views.has(parameters.get('view'))) view = parameters.get('view');
    search.value = parameters.get('q') || '';

    function hotelDay() {
        const parts = Object.fromEntries(dayFormatter.formatToParts(new Date()).map(part => [part.type, part.value]));
        return `${parts.year}-${parts.month}-${parts.day}`;
    }

    function classify() {
        currentDay = hotelDay();
        const dateLabel = root.querySelector('[data-hotel-date]');
        dateLabel.textContent = new Intl.DateTimeFormat('en-GB', {timeZone: 'Asia/Ho_Chi_Minh', weekday: 'short', day: '2-digit', month: 'short', year: 'numeric'}).format(new Date()) + ' · Vietnam time';
        rows.forEach((row, index) => {
            const status = row.dataset.status.trim().toUpperCase();
            const arrival = row.dataset.arrival;
            const departure = row.dataset.departure;
            const pending = status === 'CONFIRMED' || status === 'ASSIGNED';
            const serverReview = row.dataset.review === 'true';
            const lateDeparture = status === 'CHECKED_IN' && departure && departure < currentDay;
            const pastArrival = pending && arrival && arrival < currentDay;
            row.stay = {
                all: true,
                arrivals: pending && arrival === currentDay,
                // Match BookingTimePolicy.needsRoomAssignment on the dashboard after refresh.
                assignments: Boolean(status === 'CONFIRMED' && arrival && departure
                        && arrival >= currentDay && departure > arrival && !serverReview),
                inhouse: status === 'CHECKED_IN',
                review: Boolean(serverReview || lateDeparture || pastArrival),
                search: fold(row.dataset.search),
                phone: (row.dataset.phone || '').replace(/\D/g, ''),
                index
            };
            row.classList.toggle('stay-needs-review', row.stay.review);
            const reviewLabel = row.querySelector('[data-review-label]');
            reviewLabel.hidden = !row.stay.review;
            reviewLabel.textContent = lateDeparture ? 'Departure overdue' : serverReview || pastArrival ? 'Arrival needs review' : '';
            row.stay.priority = row.stay.review ? 0 : row.stay.arrivals ? 1 : status === 'CHECKED_IN' && departure === currentDay ? 2 : status === 'CHECKED_IN' ? 3 : 4;
        });
        const parent = root.querySelector('[data-stay-rows]');
        if (parent) rows.slice().sort((left, right) => left.stay.priority - right.stay.priority
                || (left.dataset.arrival || '9999').localeCompare(right.dataset.arrival || '9999')
                || left.dataset.booking.localeCompare(right.dataset.booking)
                || left.stay.index - right.stay.index).forEach(row => parent.appendChild(row));
        root.querySelectorAll('[data-filter-count]').forEach(counter => {
            counter.textContent = rows.filter(row => row.stay[counter.dataset.filterCount]).length;
        });
    }

    function render() {
        const query = fold(search.value);
        const phoneQuery = query.replace(/\D/g, '');
        let visible = 0;
        rows.forEach(row => {
            const matchesSearch = !query || row.stay.search.includes(query)
                    || /^\+?[\d\s.()-]+$/.test(query) && phoneQuery.length >= 3 && row.stay.phone.includes(phoneQuery);
            row.hidden = !(row.stay[view] && matchesSearch);
            if (!row.hidden) visible++;
        });
        buttons.forEach(button => button.setAttribute('aria-pressed', String(button.dataset.stayFilter === view)));
        if (table) table.hidden = visible === 0;
        empty.hidden = visible > 0;
        result.textContent = `Showing ${visible} of ${rows.length} ${rows.length === 1 ? 'booking' : 'bookings'}`;
        const url = new URL(window.location.href);
        if (view === 'all') url.searchParams.delete('view'); else url.searchParams.set('view', view);
        if (search.value.trim()) url.searchParams.set('q', search.value.trim()); else url.searchParams.delete('q');
        window.history.replaceState(null, '', url);
        root.querySelector('[data-list-refresh]').href = url.href;
    }

    buttons.forEach(button => {
        button.disabled = false;
        button.addEventListener('click', () => { view = button.dataset.stayFilter; render(); });
    });
    search.disabled = false;
    search.addEventListener('input', render);
    if (reset) reset.addEventListener('click', () => { view = 'all'; search.value = ''; render(); search.focus(); });
    root.querySelectorAll('[data-cancel-no-show]').forEach(form => form.addEventListener('submit', event => {
        const id = form.elements.bookingID.value;
        if (!window.confirm(`Cancel booking ${id} for no-show? No refund applies.`)) event.preventDefault();
    }));
    document.addEventListener('visibilitychange', () => {
        if (!document.hidden && hotelDay() !== currentDay) { classify(); render(); }
    });
    window.addEventListener('pageshow', () => { classify(); render(); });
    classify();
    render();
})();
