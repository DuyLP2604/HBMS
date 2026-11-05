// Update the room counts when reception changes the stay dates.
(function () {
    'use strict';
    const table = document.querySelector('[data-room-availability-url]');
    if (!table) return;

    const checkIn = document.getElementById('checkInDate');
    const checkOut = document.getElementById('checkOutDate');
    const status = document.getElementById('counterRoomAvailability');
    const body = table.tBodies[0];
    const rows = Array.from(body.querySelectorAll('[data-room-type]'), element => {
        const quantity = element.querySelector('input[name="quantity"]');
        return {
            element,
            id: element.dataset.roomType,
            count: element.querySelector('[data-available-rooms]'),
            quantity,
            guests: element.querySelector('input[name="guestCount"]'),
            originalMax: quantity.max
        };
    });
    let pending;

    // Keep the original order within the available and sold-out groups.
    function renderAvailability(counts) {
        const bookableRows = [];
        const soldOutRows = [];
        let clearedSelection = false;
        for (const row of rows) {
            const count = counts[row.id];
            const known = Number.isSafeInteger(count) && count >= 0;
            const soldOut = known && count === 0;
            row.count.textContent = known ? String(count) : '—';
            row.quantity.max = known ? String(Math.min(count, Number(row.originalMax))) : row.originalMax;
            row.element.classList.toggle('ui-room-sold-out', soldOut);
            // Read-only fields still submit alongside their room type.
            row.quantity.readOnly = soldOut;
            row.guests.readOnly = soldOut;
            if (soldOut) {
                row.count.textContent = '0 · Sold out';
                clearedSelection = clearedSelection || Number(row.quantity.value) > 0 || Number(row.guests.value) > 0;
                row.quantity.value = '0';
                row.guests.value = '0';
                soldOutRows.push(row.element);
            } else {
                bookableRows.push(row.element);
            }
        }

        const orderedRows = [...bookableRows, ...soldOutRows];
        if (orderedRows.some((element, index) => body.rows[index] !== element)) {
            body.append(...orderedRows);
        }
        return clearedSelection;
    }

    async function refreshAvailability() {
        if (pending) pending.abort();
        const invalidRange = checkIn.value && checkOut.value && checkOut.value <= checkIn.value;
        checkOut.setCustomValidity(invalidRange ? 'Check-out must be after check-in.' : '');
        if (!checkIn.checkValidity() || !checkOut.checkValidity()) {
            renderAvailability({});
            status.textContent = 'Choose valid check-in and check-out dates to see room availability.';
            return;
        }

        const request = new AbortController();
        pending = request;
        const url = new URL(table.dataset.roomAvailabilityUrl, document.baseURI);
        url.search = new URLSearchParams({
            action: 'roomAvailability',
            checkInDate: checkIn.value,
            checkOutDate: checkOut.value
        });
        for (const row of rows) row.count.textContent = '—';
        status.textContent = 'Checking room availability…';
        try {
            const response = await fetch(url, {signal: request.signal});
            if (!response.ok) throw new Error('Room availability request failed.');
            const counts = await response.json();
            if (request.signal.aborted) return;

            status.textContent = renderAvailability(counts)
                ? 'Selections for sold-out room types were cleared. Review the rooms before submitting.'
                : 'Availability is checked again when you submit.';
        } catch (error) {
            if (!request.signal.aborted) {
                renderAvailability({});
                status.textContent = 'Room availability could not be loaded. It will be checked when you submit.';
            }
        }
    }

    checkIn.addEventListener('change', refreshAvailability);
    checkOut.addEventListener('change', refreshAvailability);
    window.addEventListener('pageshow', event => {
        if (event.persisted) refreshAvailability();
    });
    refreshAvailability();
}());
