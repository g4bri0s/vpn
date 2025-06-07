/**
 * Função para inicializar componentes quando o documento estiver pronto
 */
document.addEventListener('DOMContentLoaded', function() {
    // Ativar tooltips do Bootstrap
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Ativar popovers do Bootstrap
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });

    // Fechar automaticamente as mensagens de alerta após 5 segundos
    var alerts = document.querySelectorAll('.alert.alert-dismissible');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Validação de formulários
    var forms = document.querySelectorAll('.needs-validation');
    Array.prototype.slice.call(forms).forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
});

/**
 * Função para confirmar exclusão
 */
function confirmDelete(event) {
    event.preventDefault();
    
    if (confirm('Tem certeza que deseja excluir este item? Esta ação não pode ser desfeita.')) {
        // Se o usuário confirmar, segue com a ação do link
        window.location.href = event.currentTarget.href;
    }
}

/**
 * Função para formatar datas
 */
function formatDate(dateString) {
    if (!dateString) return '';
    
    const options = { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    };
    
    return new Date(dateString).toLocaleDateString('pt-BR', options);
}

/**
 * Função para copiar texto para a área de transferência
 */
function copyToClipboard(text, element) {
    navigator.clipboard.writeText(text).then(function() {
        // Feedback visual
        const originalText = element.innerHTML;
        element.innerHTML = '<i class="bi bi-check"></i> Copiado!';
        
        // Volta ao texto original após 2 segundos
        setTimeout(function() {
            element.innerHTML = originalText;
        }, 2000);
    }).catch(function(err) {
        console.error('Erro ao copiar texto: ', err);
    });
}

/**
 * Função para alternar a visibilidade de senha
 */
function togglePasswordVisibility(inputId) {
    const passwordInput = document.getElementById(inputId);
    const icon = document.querySelector(`[onclick="togglePasswordVisibility('${inputId}')"] i`);
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        icon.classList.remove('bi-eye');
        icon.classList.add('bi-eye-slash');
    } else {
        passwordInput.type = 'password';
        icon.classList.remove('bi-eye-slash');
        icon.classList.add('bi-eye');
    }
}
