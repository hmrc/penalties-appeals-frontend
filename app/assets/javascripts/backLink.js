var backLinks = document.querySelectorAll('#back-link[href="#"]')
// prevent resubmit warning
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
    window.history.replaceState(null, null, window.location.href);
}

if (backLinks.length > 0) {
    backLinks.forEach(element => element.addEventListener('click', function(e) {
        e.preventDefault();
        window.history.back();
    }))
}