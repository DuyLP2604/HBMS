document.addEventListener("DOMContentLoaded", function ()
{
    const countdownElements = document.querySelectorAll(".payment-countdown");
    const reloadKey = "hbms:booking-expiration:" + window.location.pathname;
    let countdownInterval = null;
    let reloadTimeout = null;
    if (countdownElements.length === 0)
    {
        return;
    }

    function scheduleReload(expiredDeadlines)
    {
        if (expiredDeadlines.length === 0 || reloadTimeout !== null)
        {
            return;
        }
        const signature = Array.from(new Set(expiredDeadlines)).sort().join(",");
        try
        {
            if (window.sessionStorage.getItem(reloadKey) === signature)
            {
                return;
            }
            window.sessionStorage.setItem(reloadKey, signature);
        }
        catch (ex)
        {
            return;
        }
        reloadTimeout = window.setTimeout(function ()
        {
            window.location.reload();
        }, 1000);
    }

    function updateCountdowns()
    {
        const now = Date.now();
        const expiredDeadlines = [];
        let hasActiveCountdown = false;
        countdownElements.forEach(function (element)
        {
            const rawDeadline = (element.dataset.deadline || "").trim();
            const deadline = Number(rawDeadline);
            if (rawDeadline === "" || !Number.isFinite(deadline) || deadline <= 0)
            {
                element.textContent = "(Deadline unavailable)";
                return;
            }
            const remaining = deadline - now;
            if (remaining <= 0)
            {
                element.textContent = "(Expired; refresh to update)";
                expiredDeadlines.push(String(deadline));
                return;
            }
            hasActiveCountdown = true;
            const totalSeconds = Math.ceil(remaining / 1000);
            const minutes = Math.floor(totalSeconds / 60);
            const seconds = totalSeconds % 60;
            element.textContent = "(" + String(minutes).padStart(2, "0") + ":" + String(seconds).padStart(2, "0") + " remaining)";
        });
        scheduleReload(expiredDeadlines);
        if (!hasActiveCountdown && countdownInterval !== null)
        {
            window.clearInterval(countdownInterval);
            countdownInterval = null;
        }
        return hasActiveCountdown;
    }

    function startCountdowns()
    {
        if (updateCountdowns() && countdownInterval === null)
        {
            countdownInterval = window.setInterval(updateCountdowns, 1000);
        }
    }

    startCountdowns();
    document.addEventListener("visibilitychange", function ()
    {
        if (!document.hidden)
        {
            startCountdowns();
        }
    });
    window.addEventListener("pageshow", function (event)
    {
        if (event.persisted)
        {
            startCountdowns();
        }
    });
});