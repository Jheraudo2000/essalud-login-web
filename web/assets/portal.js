'use strict';
document.querySelector('[data-mostrar-clave]')?.addEventListener('click',event=>{
    const input=document.getElementById('clave');const mostrar=input.type==='password';
    input.type=mostrar?'text':'password';event.currentTarget.textContent=mostrar?'Ocultar':'Ver';
    event.currentTarget.setAttribute('aria-label',mostrar?'Ocultar contraseña':'Mostrar contraseña');
});
const ayuda=document.getElementById('ayuda');
document.querySelector('[data-ayuda]')?.addEventListener('click',()=>ayuda.showModal());
document.querySelector('[data-cerrar-ayuda]')?.addEventListener('click',()=>ayuda.close());
document.querySelector('[data-menu]')?.addEventListener('click',()=>document.body.classList.toggle('menu-abierto'));
document.querySelector('[data-imprimir]')?.addEventListener('click',()=>window.print());
document.querySelectorAll('form[data-confirmar]').forEach(form=>form.addEventListener('submit',event=>{
    if(!window.confirm(form.dataset.confirmar))event.preventDefault();
}));
document.querySelectorAll('input[name="horarios"]').forEach(check=>check.addEventListener('change',()=>{
    if(document.querySelectorAll('input[name="horarios"]:checked').length>3){check.checked=false;window.alert('Puedes seleccionar hasta tres horarios por solicitud.');}
}));
// El navegador solo permite cerrar automáticamente ventanas abiertas por un script.
// En una pestaña abierta por el usuario permanece la pantalla de bloqueo, sin el formulario.
if(document.body.hasAttribute('data-cerrar-ventana')&&window.opener)window.close();
