function addThousandSeparators(thousandSeparator, number)
{
    number += '';
    x = number.split('.');
    x1 = x[0];
    var rgx = /(\d+)(\d{3})/;
    while (rgx.test(x1)) {
        x1 = x1.replace(rgx, '$1' + thousandSeparator + '$2');
    }
    return x1;
}

function CountingUpThenAutoRefreshLabel_continueCounting(countingUpDurationSeconds, countingUpRefreshIntervalSeconds, tendencyThreshold,
            thousandSeparator, spanTag, currentDiffToTarget, targetValue) {
    spanTag.innerHTML = addThousandSeparators(thousandSeparator, targetValue - currentDiffToTarget);
    
    var m = Math.pow(tendencyThreshold, 1.0 / (countingUpDurationSeconds / countingUpRefreshIntervalSeconds));
  
    setTimeout(
        function() { CountingUpThenAutoRefreshLabel_continueCounting(
                countingUpDurationSeconds, countingUpRefreshIntervalSeconds, tendencyThreshold, 
                thousandSeparator, spanTag, m*currentDiffToTarget, targetValue); }, 
        1000*countingUpRefreshIntervalSeconds
    );
}