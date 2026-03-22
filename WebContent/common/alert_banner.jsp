<div id="alert-banner" class="alert-banner hidden"></div>
<script>
(function () {
    function pollAlert() {
        fetch('${pageContext.request.contextPath}/status?type=alert')
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.alert) {
                    var banner = document.getElementById('alert-banner');
                    banner.textContent = 'ALERT: ' + data.alert;
                    banner.classList.remove('hidden');
                }
            })
            .catch(function () {});
    }

    setInterval(pollAlert, 5000);
})();
</script>
