(function () {
    'use strict';
    const root = document.querySelector('[data-inhouse-list]');
    if (!root) return;
    const rows = Array.from(root.querySelectorAll('[data-inhouse-row]'));
    const search = root.querySelector('[data-inhouse-search]');
    const filters = Array.from(root.querySelectorAll('[data-inhouse-filter]'));
    const table = root.querySelector('[data-inhouse-table]');
    const empty = root.querySelector('[data-inhouse-empty]');
    const reset = root.querySelector('[data-inhouse-reset]');
    const result = root.querySelector('[data-inhouse-result]');
    const refresh = root.querySelector('[data-inhouse-refresh]');
    const parameters = new URL(window.location.href).searchParams;
    let view = ['all', 'today', 'overdue'].includes(parameters.get('view')) ? parameters.get('view') : 'all';
    let currentDay = '';
    const fold = value => String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[đĐ]/g, 'd').toLowerCase().replace(/\s+/g, ' ').trim();
    const dayFormat = new Intl.DateTimeFormat('en-GB', {timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit'});
    search.value = parameters.get('q') || '';

    function classify() {
        const parts = Object.fromEntries(dayFormat.formatToParts(new Date()).map(part => [part.type, part.value]));
        currentDay = parts.year + '-' + parts.month + '-' + parts.day;
        root.querySelector('[data-inhouse-date]').textContent = new Intl.DateTimeFormat('en-GB', {timeZone: 'Asia/Ho_Chi_Minh', weekday: 'short', day: '2-digit', month: 'short', year: 'numeric'}).format(new Date()) + ' · Vietnam time';
        rows.forEach((row, index) => {
            const departure = row.dataset.departure || '';
            row.stay = {all: true, today: departure === currentDay, overdue: Boolean(departure && departure < currentDay), text: fold(row.dataset.search), phone: String(row.dataset.phone || '').replace(/\D/g, ''), index};
            const status = row.querySelector('[data-inhouse-status]');
            status.textContent = row.stay.overdue ? 'Departure overdue' : row.stay.today ? 'Due today' : 'In-house';
            status.classList.toggle('guest-state-overdue', row.stay.overdue);
            status.classList.toggle('guest-state-due', row.stay.today);
        });
        const parent = root.querySelector('[data-inhouse-rows]');
        rows.slice().sort((a, b) => (a.dataset.departure || '9999').localeCompare(b.dataset.departure || '9999')
                || a.dataset.booking.localeCompare(b.dataset.booking) || a.stay.index - b.stay.index).forEach(row => parent.appendChild(row));
        root.querySelectorAll('[data-inhouse-count]').forEach(label => { label.textContent = rows.filter(row => row.stay[label.dataset.inhouseCount]).length; });
    }
    function render() {
        const query = fold(search.value);
        const phone = query.replace(/\D/g, '');
        let visible = 0;
        rows.forEach(row => {
            const matches = !query || row.stay.text.includes(query) || (/^\+?[\d\s.()-]+$/.test(query) && phone.length >= 3 && row.stay.phone.includes(phone));
            row.hidden = !row.stay[view] || !matches;
            if (!row.hidden) visible++;
        });
        filters.forEach(button => button.setAttribute('aria-pressed', String(button.dataset.inhouseFilter === view)));
        table.hidden = visible === 0;
        empty.hidden = visible > 0;
        root.querySelector('[data-inhouse-empty-title]').textContent = rows.length ? 'No matching stays' : 'No in-house guests';
        root.querySelector('[data-inhouse-empty-text]').textContent = rows.length ? 'Try another name, room or booking, or clear the filters.' : 'Guests will appear here after check-in is completed.';
        reset.hidden = rows.length === 0;
        result.textContent = 'Showing ' + visible + ' of ' + rows.length + (rows.length === 1 ? ' stay' : ' stays');
        const url = new URL(window.location.href);
        if (view === 'all') url.searchParams.delete('view'); else url.searchParams.set('view', view);
        if (search.value.trim()) url.searchParams.set('q', search.value.trim()); else url.searchParams.delete('q');
        window.history.replaceState(null, '', url);
        refresh.href = url.href;
    }
    filters.forEach(button => {
        button.disabled = false;
        button.addEventListener('click', () => { view = button.dataset.inhouseFilter; render(); });
    });
    search.disabled = false;
    search.addEventListener('input', render);
    reset.addEventListener('click', () => { view = 'all'; search.value = ''; render(); search.focus(); });
    root.querySelectorAll('[data-open-stay]').forEach(button => {
        const dialog = document.getElementById(button.dataset.openStay);
        button.addEventListener('click', () => {
            if (!dialog.open) dialog.showModal();
        });
        dialog.addEventListener('close', () => button.focus());
        dialog.addEventListener('click', event => {
            if (event.target === dialog) dialog.close();
        });
    });
    root.querySelectorAll('[data-close-stay]').forEach(button => button.addEventListener('click', () => button.closest('dialog').close()));
    document.addEventListener('visibilitychange', () => { if (!document.hidden) { classify(); render(); } });
    window.addEventListener('pageshow', () => { classify(); render(); });
    classify();
    render();
}());
