/* ============================================
   Search Box Enhancement Script
   Adds visual feedback and improved UX
   ============================================ */

$(document).ready(function() {
    // Add helper text below search box
    const searchContainer = $('.sidebar .input');
    
    // Add helper text if it doesn't exist
    if (!searchContainer.find('.search-helper').length) {
        searchContainer.append(`
            <div class="search-helper" style="
                margin-top: 8px;
                font-size: 13px;
                color: var(--gray-500);
                padding-left: 4px;
                text-align: left;
                transition: all 0.3s ease;
            ">
                Enter at least 3 characters to search (e.g., CSC, MAT, ECO)
            </div>
        `);
    }
    
    // Track input changes for better feedback
    const originalInputHandler = $("#select-state-selectized").on("input");
    
    $("#select-state-selectized").on("input", function() {
        const input = $(this).val();
        const helper = $('.search-helper');
        
        if (input.length === 0) {
            helper.text('Enter at least 3 characters to search (e.g., CSC, MAT, ECO)');
            helper.css('color', 'var(--gray-500)');
        } else if (input.length < 3) {
            helper.text(`Type ${3 - input.length} more character${3 - input.length > 1 ? 's' : ''} to search...`);
            helper.css('color', 'var(--gray-400)');
        } else {
            helper.text('Searching for courses...');
            helper.css('color', 'var(--uoft-blue)');
            
            // Reset helper text after search completes
            setTimeout(() => {
                if ($('.selectize-dropdown .option').length > 0) {
                    helper.text(`Found ${$('.selectize-dropdown .option').length} courses`);
                    helper.css('color', 'var(--gray-500)');
                } else {
                    helper.text('No courses found. Try a different search term.');
                    helper.css('color', 'var(--gray-400)');
                }
            }, 500);
        }
    });
    
    // Clear helper text when an item is selected
    selectControl.on('item_add', function() {
        $('.search-helper').text('Course added! Search for another course...');
        $('.search-helper').css('color', 'var(--gray-500)');
        
        setTimeout(() => {
            $('.search-helper').text('Enter at least 3 characters to search (e.g., CSC, MAT, ECO)');
        }, 2000);
    });
    
    // Add pulse animation when focused
    $("#select-state-selectized").on("focus", function() {
        $(this).parent().addClass('pulse-focus');
    }).on("blur", function() {
        $(this).parent().removeClass('pulse-focus');
    });
    
    // Division badge color assignment based on campus
    $(document).on('DOMNodeInserted', '.selectize-dropdown', function() {
        $('.division-badge').each(function() {
            const text = $(this).text();
            if (text === 'UTSG') {
                $(this).css({
                    'background': 'linear-gradient(135deg, #E3F2FD, #BBDEFB)',
                    'border-color': '#90CAF9',
                    'color': '#1565C0'
                });
            } else if (text === 'UTSC') {
                $(this).css({
                    'background': 'linear-gradient(135deg, #F3E5F5, #E1BEE7)',
                    'border-color': '#CE93D8',
                    'color': '#6A1B9A'
                });
            } else if (text === 'UTM') {
                $(this).css({
                    'background': 'linear-gradient(135deg, #E8F5E9, #C8E6C9)',
                    'border-color': '#A5D6A7',
                    'color': '#2E7D32'
                });
            }
        });
    });
});

// Add CSS for pulse animation
const style = document.createElement('style');
style.textContent = `
    .pulse-focus {
        animation: pulse 2s infinite;
    }
    
    @keyframes pulse {
        0% {
            box-shadow: 0 0 0 0 rgba(0, 42, 92, 0.2);
        }
        70% {
            box-shadow: 0 0 0 10px rgba(0, 42, 92, 0);
        }
        100% {
            box-shadow: 0 0 0 0 rgba(0, 42, 92, 0);
        }
    }
    
`;
document.head.appendChild(style);