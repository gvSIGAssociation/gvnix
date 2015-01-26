var resultado;
function cargarAppletSafe(tipoAcceso) {
	//-- Si la version de Java no es superior a la 1.6.0_10 mostrar mensaje
	if (deployJava.versionCheck("1.6.0_10+")) {
		if (tipoAcceso == "LDAP") {
			loadAutenticacionCompletaApplet("ldap","f","datosgenerados", "", "true", "", "", "", "", "","");
		} else {
			loadAutenticacionSimpleApplet(tipoAcceso,
					"datosgenerados", "");
		}
	} else {

		alert("La version de java debe ser mayor de la 1.6");
	}
}

function appletFinalizado(s, codigoError) {
	resultado = s;
	if (resultado == null || resultado == "null") {
		showInfoSafe();
	} else {
		var oFormObject = jQuery("#form-signin-certificate")[0];
		oFormObject.elements["j_token"].value = resultado;
		oFormObject.submit();
	}
}

function showInfoSafe() {
	var infoSafe = document.getElementById("infoSafe");
	infoSafe.style.display = 'inline';
}
