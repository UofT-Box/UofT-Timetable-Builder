/* ============================================
   Course List Enhancement Script
   Adds modern interactions and animations
   ============================================ */

$(document).ready(function() {
    // Add credit display enhancement
    enhanceCreditDisplay();
    
    // Monitor course list changes for animations
    observeCourseListChanges();
});

/**
 * Enhance credit display with animation
 */
function enhanceCreditDisplay() {
    // Don't add any prefix since the original function handles the display
    // Just enhance the visual appearance when it changes
    
    // Watch for changes and add animation
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === 'childList' || mutation.type === 'characterData') {
                const $credit = $(mutation.target).closest('.credit');
                if ($credit.length) {
                    // Add a subtle pulse animation when credits change
                    $credit.addClass('credit-update');
                    setTimeout(() => {
                        $credit.removeClass('credit-update');
                    }, 300);
                }
            }
        });
    });

    $('.credit').each(function() {
        observer.observe(this, { childList: true, characterData: true, subtree: true });
    });
}

/**
 * Observe course list for changes and add animations
 */
function observeCourseListChanges() {
    const courseContainers = ['#fall-courses', '#winter-courses'];
    
    courseContainers.forEach(containerId => {
        const container = document.querySelector(containerId);
        if (!container) return;
        
        // Create observer for course additions
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                mutation.addedNodes.forEach((node) => {
                    if (node.classList && node.classList.contains('course-item')) {
                        // Add entrance animation
                        enhanceCourseItem(node);
                    }
                });
            });
        });
        
        observer.observe(container, { childList: true });
    });
}

/**
 * Enhance individual course items
 */
function enhanceCourseItem(courseItem) {
    const $item = $(courseItem);
    
    // Add data attributes for better styling
    const courseCode = $item.attr('id');
    if (courseCode) {
        // Detect if it's a year-long course
        const lastChar = courseCode.slice(-1);
        if (lastChar === 'Y') {
            $item.attr('data-term', 'Y');
        }
        
        // Add credit info as data attribute
        const isHalf = lastChar === 'F' || lastChar === 'S';
        $item.find('.course-name').attr('data-credits', isHalf ? '0.5' : '1.0');
    }
    
    // Enhance button interactions
    enhanceButtons($item);
    
    // Add long-press for lock functionality (future feature)
    addLongPressSupport($item);
}

/**
 * Enhance edit and delete buttons
 */
function enhanceButtons($item) {
    // Replace emoji with better icons if needed
    const editBtn = $item.find('.edit-btn');
    const deleteBtn = $item.find('.delete-btn');
    
    // Add tooltips
    editBtn.attr('title', 'Edit course schedule');
    deleteBtn.attr('title', 'Remove course');
    
    // Add click feedback
    editBtn.on('click', function(e) {
        $(this).addClass('clicked');
        setTimeout(() => $(this).removeClass('clicked'), 300);
    });
    
    deleteBtn.on('click', function(e) {
        // Add removal animation
        $item.css({
            'transform': 'translateX(-100%)',
            'opacity': '0',
            'transition': 'all 0.3s ease'
        });
        
        // Original delete will be called after animation
        setTimeout(() => {
            // Original onclick handler will execute
        }, 300);
    });
}

/**
 * Add long-press support for locking courses
 */
function addLongPressSupport($item) {
    let pressTimer;
    const longPressDuration = 800;
    
    $item.on('mousedown touchstart', function(e) {
        if ($(e.target).hasClass('edit-btn') || $(e.target).hasClass('delete-btn')) {
            return;
        }
        
        pressTimer = setTimeout(() => {
            // Toggle locked state
            $item.toggleClass('locked');
            
            // Haptic feedback for mobile
            if (window.navigator && window.navigator.vibrate) {
                window.navigator.vibrate(50);
            }
            
            // Visual feedback
            $item.css('transform', 'scale(0.98)');
            setTimeout(() => {
                $item.css('transform', '');
            }, 200);
        }, longPressDuration);
    });
    
    $item.on('mouseup mouseleave touchend touchcancel', function() {
        clearTimeout(pressTimer);
    });
}

// Add ripple effect styles
const style = document.createElement('style');
style.textContent = `
    .ripple {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.6);
        transform: scale(0);
        animation: ripple-animation 0.6s ease-out;
        pointer-events: none;
    }
    
    @keyframes ripple-animation {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
    
    .term-btn {
        position: relative;
        overflow: hidden;
    }
    
    .clicked {
        animation: button-click 0.3s ease;
    }
    
    @keyframes button-click {
        0%, 100% {
            transform: scale(1);
        }
        50% {
            transform: scale(0.95);
        }
    }
    
    /* Course item entrance animation */
    .course-item {
        animation: slideInFade 0.4s ease-out;
    }
    
    @keyframes slideInFade {
        from {
            opacity: 0;
            transform: translateY(10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
`;
document.head.appendChild(style);

// Export functions for external use
window.enhanceCourseItem = enhanceCourseItem;