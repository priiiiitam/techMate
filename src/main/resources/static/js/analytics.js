document.addEventListener("DOMContentLoaded", function() {
    const dataEl = document.getElementById("analyticsData");
    
    const totalPresent = parseInt(dataEl.getAttribute("data-present")) || 0;
    const totalAbsent = parseInt(dataEl.getAttribute("data-absent")) || 0;
    
    // Parse HashMaps provided by Thymeleaf as string representation
    const rawStudentMap = dataEl.getAttribute("data-studentMap") || "{}";
    const cleanStudentMapStr = rawStudentMap.replace(/([a-zA-Z0-9 ]+)=([0-9.]+)/g, '"$1":$2');
    let studentMap = {};
    try {
        studentMap = JSON.parse(cleanStudentMapStr);
    } catch(e) { }

    // Enhanced Pie Chart
    const ctxPie = document.getElementById('pieChart').getContext('2d');
    new Chart(ctxPie, {
        type: 'doughnut',
        data: {
            labels: ['Present/Leave', 'Absent'],
            datasets: [{
                data: [totalPresent, totalAbsent],
                backgroundColor: [
                    'linear-gradient(145deg, #10b981, #059669)',
                    'linear-gradient(145deg, #ef4444, #dc2626)'
                ],
                borderWidth: 0,
                hoverOffset: 8
            }]
        },
        options: { 
            responsive: true, 
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 25,
                        usePointStyle: true,
                        font: { size: 13, family: 'Inter' },
                        color: '#64748b'
                    }
                }
            },
            animation: { animateRotate: true, duration: 1500 }
        }
    });

    // Enhanced Bar Chart
    const ctxBar = document.getElementById('barChart').getContext('2d');
    const labels = Object.keys(studentMap);
    const dataPoints = Object.values(studentMap);

    new Chart(ctxBar, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Attendance %',
                data: dataPoints,
                backgroundColor: '#6366f1',
                borderRadius: 8,
                borderSkipped: false,
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            scales: { 
                y: { 
                    beginAtZero: true, 
                    max: 100,
                    grid: { color: 'rgba(0,0,0,0.04)' },
                    ticks: { callback: (v) => v + '%', font: { family: 'Inter' } }
                },
                x: { grid: { display: false } }
            },
            plugins: { legend: { display: false } },
            animation: { duration: 1500, easing: 'easeOutQuart' }
        }
    });

    // Staggered entrance animation
    document.querySelectorAll('.stat-card, .chart-box').forEach((el, i) => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        setTimeout(() => {
            el.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
            el.style.opacity = '1';
            el.style.transform = 'translateY(0)';
        }, i * 150);
    });
});