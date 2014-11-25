/*! ghembedder - v0.1.0 - 2012-08-28
* https://github.com/kirbysayshi/ghembedder
* Copyright (c) 2012 Andrew Petersen; Licensed MIT */

/*
 * Copyright (c) 2010 Nick Galbreath
 * http://code.google.com/p/stringencoders/source/browse/#svn/trunk/javascript
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

/* base64 encode/decode compatible with window.btoa/atob
 *
 * window.atob/btoa is a Firefox extension to convert binary data (the "b")
 * to base64 (ascii, the "a").
 *
 * It is also found in Safari and Chrome.  It is not available in IE.
 *
 * if (!window.btoa) window.btoa = base64.encode
 * if (!window.atob) window.atob = base64.decode
 *
 * The original spec's for atob/btoa are a bit lacking
 * https://developer.mozilla.org/en/DOM/window.atob
 * https://developer.mozilla.org/en/DOM/window.btoa
 *
 * window.btoa and base64.encode takes a string where charCodeAt is [0,255]
 * If any character is not [0,255], then an DOMException(5) is thrown.
 *
 * window.atob and base64.decode take a base64-encoded string
 * If the input length is not a multiple of 4, or contains invalid characters
 *   then an DOMException(5) is thrown.
 */
;(function(window) {

var base64 = window.base64 = {};
base64.PADCHAR = '=';
base64.ALPHA = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';

base64.makeDOMException = function() {
	// sadly in FF,Safari,Chrome you can't make a DOMException

	// not available, just passback a duck-typed equiv
	// https://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Error
	// https://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Error/prototype
	var ex = new Error("DOM Exception 5");

	// ex.number and ex.description is IE-specific.
	ex.code = ex.number = 5;
	ex.name = ex.description = "INVALID_CHARACTER_ERR";

	// Safari/Chrome output format
	ex.toString = function() { return 'Error: ' + ex.name + ': ' + ex.message; };
	return ex;
};

base64.getbyte64 = function(s,i) {
	// This is oddly fast, except on Chrome/V8.
	//  Minimal or no improvement in performance by using a
	//   object with properties mapping chars to value (eg. 'A': 0)
	var idx = base64.ALPHA.indexOf(s.charAt(i));
	if (idx === -1) {
		throw base64.makeDOMException();
	}
	return idx;
};

base64.decode = function(s) {
	// convert to string
	s = '' + s;
	var getbyte64 = base64.getbyte64;
	var pads, i, b10;
	var imax = s.length;
	if (imax === 0) {
		return s;
	}

	if (imax % 4 !== 0) {
		throw base64.makeDOMException();
	}

	pads = 0;
	if (s.charAt(imax - 1) === base64.PADCHAR) {
		pads = 1;
		if (s.charAt(imax - 2) === base64.PADCHAR) {
			pads = 2;
		}
		// either way, we want to ignore this last block
		imax -= 4;
	}

	var x = [];
	for (i = 0; i < imax; i += 4) {
		b10 = (getbyte64(s,i) << 18) | (getbyte64(s,i+1) << 12) |
			(getbyte64(s,i+2) << 6) | getbyte64(s,i+3);
		x.push(String.fromCharCode(b10 >> 16, (b10 >> 8) & 0xff, b10 & 0xff));
	}

	switch (pads) {
	case 1:
		b10 = (getbyte64(s,i) << 18) | (getbyte64(s,i+1) << 12) | (getbyte64(s,i+2) << 6);
		x.push(String.fromCharCode(b10 >> 16, (b10 >> 8) & 0xff));
		break;
	case 2:
		b10 = (getbyte64(s,i) << 18) | (getbyte64(s,i+1) << 12);
		x.push(String.fromCharCode(b10 >> 16));
		break;
	}
	return x.join('');
};

base64.getbyte = function(s,i) {
	var x = s.charCodeAt(i);
	if (x > 255) {
		throw base64.makeDOMException();
	}
	return x;
};

base64.encode = function(s) {
	if (arguments.length !== 1) {
		throw new SyntaxError("Not enough arguments");
	}
	var padchar = base64.PADCHAR;
	var alpha   = base64.ALPHA;
	var getbyte = base64.getbyte;

	var i, b10;
	var x = [];

	// convert to string
	s = '' + s;

	var imax = s.length - s.length % 3;

	if (s.length === 0) {
		return s;
	}
	for (i = 0; i < imax; i += 3) {
		b10 = (getbyte(s,i) << 16) | (getbyte(s,i+1) << 8) | getbyte(s,i+2);
		x.push(alpha.charAt(b10 >> 18));
		x.push(alpha.charAt((b10 >> 12) & 0x3F));
		x.push(alpha.charAt((b10 >> 6) & 0x3f));
		x.push(alpha.charAt(b10 & 0x3f));
	}
	switch (s.length - imax) {
	case 1:
		b10 = getbyte(s,i) << 16;
		x.push(alpha.charAt(b10 >> 18) + alpha.charAt((b10 >> 12) & 0x3F) +
			padchar + padchar);
		break;
	case 2:
		b10 = (getbyte(s,i) << 16) | (getbyte(s,i+1) << 8);
		x.push(alpha.charAt(b10 >> 18) + alpha.charAt((b10 >> 12) & 0x3F) +
			alpha.charAt((b10 >> 6) & 0x3f) + padchar);
		break;
	}
	return x.join('');
};

}(window));

if (!window.btoa) { window.btoa = window.base64.encode; }
if (!window.atob) { window.atob = window.base64.decode; }

///////////////////////////////////////////////////////////////////////////////
// Begin the actual ghembedder code...
///////////////////////////////////////////////////////////////////////////////

;(function(exports) {

var ghe = exports.ghe = {
	 _apiBase: 'https://api.github.com'
	,_callbacks: {}
	,_library: {}
	,_rLeadSlash: /^\/+|\/+$/g
	,_rWhiteSpace: /\s/g
};

ghe._decodeContent = function( content ){
	var decoded = window.atob( content.replace( ghe._rWhiteSpace, '' ) );

	return decoded;
};

ghe._keygen = function(){
	return 'ghe_' + ~~(Math.random() * 100000);
};

ghe._jsonpCallback = function(key){
	return ghe._callbacks[key] = function(resp){
		var  lib = ghe._library[key]
			,linenos = false
			,hasLineRange = lib.lineBegin > -1 && lib.lineEnd > -1
			,decoded
			,lines
			,nums
			,tabSpace = new Array(lib.tabSize + 1).join(' ');

		if( resp.data && resp.data.content ){

			lib.data = resp.data;

			decoded = ghe._decodeContent( resp.data.content );
			//check if the file is htm(l)
			if (lib.fileName.match(/.*\.htm[l]*$/)){
				//replace the tags so that they will be interpreted as text, and not source
				decoded = decoded.replace(/</g,"&lt;").replace(/>/g,"&gt;");
			}
			lines = decoded.split('\n');

			if( hasLineRange ){
				lines = lines.splice( lib.lineBegin - 1, lib.lineEnd - lib.lineBegin + 1 );
			}

			if( lib.linenos ){
				linenos = hasLineRange
					? lib.lineBegin
					: lib.linenos;
			}

			// apply an anchor to each line, to be able to link to specifics
			lines = lines.map(function(l, i){
				return '<a class="nocode" id="' + lib.fileName + '-L'
					+ (i + lib.lineBegin) + '">'
					+ ( l ? '' : ' ' ) + '</a>'
					+ l.replace(/\t/gi, tabSpace);
			});

			decoded = lines.join('\n');

			if(exports.prettyPrintOne){
				decoded = exports.prettyPrintOne( decoded, lib.lang, linenos );
			}

			lib.el.className += ' ghe';

			lib.el.innerHTML = '<pre class="prettyprint"><code>'
				+ decoded
				+ '</code></pre>'
				+ (lib.annotate
					 ? ghe._annotation(key)
					 : '');
		}
	};
};

ghe._annotation = function( key ){
	var  lib = ghe._library[key]
			,hasLineRange = lib.lineBegin > -1 && lib.lineEnd > -1;

	return '<div class="ghe-annotation">'
		+ lib.fileName
		+ (hasLineRange
			 ? ', lines ' + lib.lineBegin + '-' + lib.lineEnd
			 : '')
		+ (lib.data
			 ? '. <a href="' + lib.data._links.html + '" target="_blank">Source</a>'
			 : '')
		+ '</div>';

};

ghe._jsonp = function(fileUrl, cbName){
	var script = document.createElement('script');
	script.async = true;
	script.src = fileUrl
		+ (fileUrl.indexOf('?') > -1 ? '&' : '?')
		+ 'callback=' + cbName;

	document.getElementsByTagName('head')[0].appendChild(script);
};

ghe._parseNode = function(el){

	var lines = el.getAttribute('data-ghlines')
		,path = el.getAttribute('data-ghpath')
		,start
		,end;

	if(lines && lines.indexOf('-') > -1){
		lines = lines.split('-');
		start = parseInt( lines[0], 10 );
		end = parseInt( lines[1], 10 );
	} else if( lines ){
		start = end = parseInt( lines, 10 );
	} else {
		start = end = -1;
	}

	return {
		 path: path
		,userrepo: el.getAttribute('data-ghuserrepo')
		,ref: el.getAttribute('data-ghref') || 'master'
		,lineBegin: start
		,lineEnd: end
		,el: el
		,fileName: path.split('/').pop()
		,lang: el.getAttribute('data-ghlang')
		// "true" or ""/non-specified
		,linenos: el.getAttribute('data-ghlinenos')
		// "true" or ""/non-specified
		,annotate: el.getAttribute('data-ghannotate')
		,tabSize: parseInt( el.getAttribute('data-ghtabsize'), 10 ) || 4
	};
};

///////////////////////////////////////////////////////////////////////////////
// load: given a configuration object OR DOM Node that has the proper
// attributes, load the requested github file and display it.
///////////////////////////////////////////////////////////////////////////////
ghe.load = function(cfg){

	var key = ghe._keygen();

	if( cfg.nodeName ){
		cfg = ghe._parseNode(cfg);
	}

	ghe._jsonpCallback(key);
	ghe._library[key] = cfg;
	ghe._jsonp(
		ghe._apiBase
			+ '/repos/'
			+ cfg.userrepo.replace(ghe._rLeadSlash, '')
			+ '/contents/'
			+ cfg.path.replace(ghe._rLeadSlash, '')
			+ '?ref=' + cfg.ref
		,'ghe._callbacks.' + key
	);
};

///////////////////////////////////////////////////////////////////////////////
// Look through the DOM for any nodes matching [data-ghpath], and automatically
// load them as embedded github files.
///////////////////////////////////////////////////////////////////////////////
ghe.autoload = function(){
	var nodes;

	if( window.jQuery ){
		nodes = window.jQuery('[data-ghpath]');
	} else {
		nodes = document.querySelectorAll('[data-ghpath]');
	}

	for(var i = 0; i < nodes.length; i++){
		ghe.load(nodes[i]);
	}
};

}(typeof window === 'object' && window || this));