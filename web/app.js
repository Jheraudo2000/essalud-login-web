'use strict';
// La vista usa la API Java. La clave nunca se almacena en el navegador.
const roles = {
 paciente: ['Consultar especialidades y horarios disponibles','Reservar y consultar citas médicas','Cancelar, reprogramar y revisar recordatorios'],
 operador: ['Consultar citas de pacientes','Apoyar actualizaciones de citas autorizadas','Revisar el estado de las solicitudes'],
 medico: ['Consultar la agenda diaria','Revisar horarios y estados de citas','Consultar datos mínimos de las citas'],
 admin: ['Mantener especialidades, médicos y horarios','Administrar roles y parámetros','Revisar eventos relevantes del sistema']
};
const examples = {paciente:'Paciente2026!',operador:'Operador2026!',medico:'Medico2026!',admin:'Admin2026!'};
async function api(path,options={}) {
 const r=await fetch(path,{credentials:'same-origin',cache:'no-store',...options});
 const data=await r.json();
 if(!r.ok) { const error=new Error(data.error || 'No se pudo completar la solicitud.'); error.status=r.status; throw error; }
 return data;
}
function setError(id,message){const el=document.getElementById(id);el.textContent=message;el.hidden=!message;}
if(document.body.dataset.page==='login') {
 const form=document.getElementById('login-form'); const submit=document.getElementById('submit-login');
 api('/api/sesion').then(()=>location.replace('/inicio')).catch(e=>{if(e.status!==401)setError('login-error','No se pudo conectar con el servidor. Intenta nuevamente.');});
 document.querySelectorAll('[data-demo]').forEach(button=>button.addEventListener('click',()=>{
  const user=button.dataset.demo; form.usuario.value=user;form.clave.value=examples[user];setError('login-error','');
  document.querySelectorAll('[data-demo]').forEach(b=>b.setAttribute('aria-pressed',String(b===button)));submit.focus();
 }));
 form.addEventListener('submit',async e=>{e.preventDefault();submit.disabled=true;submit.textContent='Ingresando…';setError('login-error','');
  try{await api('/api/login',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams({usuario:form.usuario.value.trim(),clave:form.clave.value})});form.clave.value='';location.replace('/inicio');}
  catch(error){setError('login-error',error instanceof TypeError?'No se pudo conectar. Revisa tu conexión e intenta nuevamente.':error.message);submit.disabled=false;submit.innerHTML='Iniciar sesión <span aria-hidden="true">→</span>';}
 });
 const dialog=document.getElementById('help-dialog');document.querySelector('.help-button').addEventListener('click',()=>dialog.showModal());
 document.querySelectorAll('.close-dialog,.close-help').forEach(b=>b.addEventListener('click',()=>dialog.close()));
} else {
 const load=async()=>{try{const user=await api('/api/sesion');document.getElementById('user-first').textContent=user.nombre.split(' ')[0];document.getElementById('user-name').textContent=user.nombre;document.getElementById('user-id').textContent=user.usuario;document.getElementById('user-role').textContent=user.rol;document.getElementById('role-badge').textContent='Acceso de '+user.rol.toLowerCase();document.getElementById('avatar').textContent=user.nombre.split(' ').slice(0,2).map(s=>s[0]).join('');const list=document.getElementById('permissions');list.replaceChildren();roles[user.usuario].forEach(text=>{const li=document.createElement('li');li.textContent=text;list.append(li);});document.getElementById('dashboard').hidden=false;setError('home-error','');}catch(error){document.getElementById('dashboard').hidden=true;if(error.status===401)location.replace('/');else setError('home-error','No se pudo verificar tu sesión. Recarga la página para intentar nuevamente.');}};
 load();window.addEventListener('pageshow',e=>{if(e.persisted)load();});const timer=setInterval(load,60000);
 document.getElementById('logout').addEventListener('click',async e=>{e.currentTarget.disabled=true;try{await api('/api/logout',{method:'POST'});clearInterval(timer);location.replace('/');}catch(error){e.currentTarget.disabled=false;setError('home-error','No se pudo cerrar la sesión. Intenta nuevamente.');}});
}
