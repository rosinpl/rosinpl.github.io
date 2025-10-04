// box rotacional home

$("#fotoVideoBox > div:gt(0)").hide();
setInterval(function() { 
  $('#fotoVideoBox > div:first')
	.hide()
	.next()
	.show()
	.end()
	.appendTo('#fotoVideoBox');
},  4000);

boxFotosVideosalternaImagens = function(id,tipo){
    var v_divfotogrande = document.getElementById('divfotogrande'+tipo+id);
    var v_boldfotopequena = document.getElementById('boldfotopequena'+tipo+id);

    for(i=1;i<4;i++){
        var divfotogrande = document.getElementById('divfotogrande'+tipo+i);
        var boldfotopequena = document.getElementById('boldfotopequena'+tipo+i);
        var tdfotopequena = document.getElementById('tdfotopequena'+tipo+i);

        if(divfotogrande){
            divfotogrande.style.display = 'none';
        }
        if(boldfotopequena){
            if(navigator.appName == "Microsoft Internet Explorer"){
                boldfotopequena.className = 'innerborder';
            } else{
                boldfotopequena.setAttribute('class', 'innerborder');
            }
        }
    }

//      v_divfotogrande.style.display = 'block';
    var obj_divfotogrande = $('#divfotogrande'+tipo+id)
    obj_divfotogrande.fadeIn()

    if(navigator.appName == "Microsoft Internet Explorer"){
        v_boldfotopequena.className = 'innerborder selected';
    } else{
        v_boldfotopequena.setAttribute('class', 'innerborder selected');
    }
}

boxFotosVideosalternaTipos = function(tipo){
    var portletvideo = document.getElementById('portletBoxVideo');
    var portletfoto = document.getElementById('portletBoxFoto');
    if(tipo == 'video'){
        portletvideo.style.display = 'block';
        portletfoto.style.display = 'none';
    } else{
        portletvideo.style.display = 'none';
        portletfoto.style.display = 'block';
    }
    boxFotosVideosalternaImagens(1,tipo);
}

boxNoticiasTrocaAba = function(tipo){
    var abaUltimas = document.getElementById('li_noticias_ultimas');
    var abaAcessadas = document.getElementById('li_noticias_acessadas');
    var divUltimas = document.getElementById('div_noticias_ultimas');
    var divAcessadas = document.getElementById('div_noticias_acessadas');
    if(tipo == 1){
        if(navigator.appName == "Microsoft Internet Explorer"){
            abaUltimas.className = 'selected';
        } else{
            abaUltimas.setAttribute('class', 'selected');
        }
        if(navigator.appName == "Microsoft Internet Explorer"){
            abaAcessadas.className = '';
        } else{
            abaAcessadas.setAttribute('class', '');
        }
        divUltimas.style.display = 'block';
        divAcessadas.style.display = 'none';
    } else{
        if(navigator.appName == "Microsoft Internet Explorer"){
            abaUltimas.className = '';
        } else{
            abaUltimas.setAttribute('class', '');
        }
        if(navigator.appName == "Microsoft Internet Explorer"){
            abaAcessadas.className = 'selected';
        } else{
            abaAcessadas.setAttribute('class', 'selected');
        }
        divUltimas.style.display = 'none';
        divAcessadas.style.display = 'block';
    }
}

function detalhes (ric){
	var j = window.open("detalhe_acao_bovespa_"+ric+".html" , "nome" , "status , scrollbars=yes ,width=421, height=570 , top=0 , left=0"); 
	j.focus();
}

function mudagrafico(pagina,periodo){
 
	//grafico.location.href=pagina
 
	document.getElementById("grafico").src = pagina

}	


function pegabox(box){
	 location.href="http://192.168.11.6/reuters/boxes/"+box+".html"
}	

function mudagrafico2(url){
		alert(url)
		 if (url == "box_grafico_petroleo_ano.html"){
		 	titulo =  "Petróleo"
		 }
		 if (url == "box_grafico_ouro_ano.html"){
		 	titulo =  "Ouro"
		 }
		 
		document.grafico.location.href="boxes/"+url
		document.getElementById("titulografico").innerHTML=titulo
		
	}	



 function verificabusca(mform){
  if(mform.q.value == ""){
   alert("Por favor, preencha o campo busca!")
   mform.q.focus();
   return false;
  }
  marcado = 1
  for (i=0; i<mform.resp.length; i++) {
   if (mform.resp[i].checked) {
    marcado = i
   }
  }
  if (marcado == 0){
   mform.action = "http://www.em.com.br/outros/resultado_busca/index.shtml";
   mform.cx.value = "017601035859718093098:0v9bdricezm";
  }else{
   if (marcado == 1){
     mform.action = "http://buscaem.estaminas.com.br/searchm.php";
     mform.query.value = mform.q.value;
    }else{
	mform.action = "http://www.em.com.br/outros/resultado_busca/index.shtml";
	mform.cx.value = "017601035859718093098:wigpczerip8";
    

    }
        }
        return(true);
  }
 
