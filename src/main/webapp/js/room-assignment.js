(() => {
    'use strict';
    const form = document.querySelector('[data-room-assignment]');
    if (!form) return;
    const groups = Array.from(form.querySelectorAll('[data-room-group]'));
    const button = form.querySelector('[data-assignment-submit]');
    const error = form.querySelector('[data-assignment-error]');
    const progress = form.querySelector('[data-selection-progress]');
    const requiredTotal = groups.reduce((sum, group) => sum + Number(group.dataset.required), 0);

    function selected(group) { return Array.from(group.querySelectorAll('input[type="checkbox"]:checked')); }

    function update() {
        const chosen = groups.flatMap(selected);
        const occupied = new Set(chosen.map(input => input.value.trim()));
        let complete = groups.length > 0 && requiredTotal > 0 && chosen.length === occupied.size;
        groups.forEach(group => {
            const picks = selected(group);
            const needed = Number(group.dataset.required);
            const valid = Number.isInteger(needed) && needed > 0 && picks.length === needed;
            complete = complete && valid;
            group.classList.toggle('room-type-complete', valid);
            group.querySelector('[data-group-count]').textContent = `Selected ${picks.length} of ${needed}`;
            const summary = Array.from(form.querySelectorAll('[data-selected-summary]')).find(item => item.dataset.selectedSummary === group.dataset.detailId);
            summary.textContent = picks.length ? picks.map(input => `Room ${input.dataset.roomNumber}`).join(', ') : 'Not selected';
            summary.classList.toggle('has-selection', picks.length > 0);
            group.querySelectorAll('input[type="checkbox"]').forEach(input => {
                input.disabled = !input.checked && (picks.length >= needed || occupied.has(input.value.trim()));
                input.closest('.room-choice').classList.toggle('room-choice-disabled', input.disabled);
                input.closest('.room-choice').querySelector('[data-room-state]').textContent = input.checked ? 'Selected' : 'Available';
            });
        });
        form.querySelector('[data-total-selection]').textContent = `${chosen.length} of ${requiredTotal} rooms selected`;
        progress.value = chosen.length;
        button.disabled = !complete || form.dataset.submitting === 'true';
        return complete;
    }

    form.addEventListener('change', event => {
        const input = event.target;
        if (!input.matches('input[type="checkbox"]')) return;
        const group = input.closest('[data-room-group]');
        const duplicate = input.checked && groups.flatMap(selected).some(other => other !== input && other.value.trim() === input.value.trim());
        if (duplicate || selected(group).length > Number(group.dataset.required)) {
            input.checked = false;
            error.textContent = duplicate ? 'This room is already selected for another room type.' : 'Select only the requested number of rooms for this room type.';
            error.hidden = false;
        } else {
            error.hidden = true;
        }
        update();
    });

    form.addEventListener('submit', event => {
        if (form.dataset.submitting === 'true') { event.preventDefault(); return; }
        if (!update()) {
            event.preventDefault();
            error.textContent = 'Select the exact number of rooms for every requested room type.';
            error.hidden = false;
            error.scrollIntoView({block: 'center'});
            return;
        }
        form.dataset.submitting = 'true';
        button.disabled = true;
        button.textContent = 'Saving assignment…';
    });
    form.querySelector('[data-refresh-rooms]').addEventListener('click', event => {
        if (groups.some(group => selected(group).length) && !window.confirm('Refresh availability and clear your room selection?')) event.preventDefault();
    });
    window.addEventListener('pageshow', () => {
        delete form.dataset.submitting;
        button.textContent = 'Confirm assignment';
        update();
    });
    update();
})();
