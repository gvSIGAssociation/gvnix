<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="html"/>
	
	<xsl:template match="/">
		<html>
			<xsl:apply-templates/>
		</html>
	</xsl:template>
	
	<xsl:template match="repository">
		
		<head>
			<META HTTP-EQUIV="Content-Type"
				CONTENT="text/html; charset=iso-8859-1"/>
			<title>
				<xsl:value-of select="@name"/>
			</title>
						
			<!-- =========================================================== -->
			<!-- CSS resources			                                     -->
			<!-- =========================================================== -->
			<link href="gvnix.css" type="text/css" rel="stylesheet"/>
			<link href="http://www.gvnix.org/assets/themes/gvnix-omega-multilanguage/resources/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
			<link href="http://www.gvnix.org/assets/themes/gvnix-omega-multilanguage/resources/font-awesome/css/font-awesome.css" rel="stylesheet" type="text/css" />
			<link href="http://www.gvnix.org/assets/themes/gvnix-omega-multilanguage/css/style.css" type="text/css" rel="stylesheet"/>

		</head>

		<body>

			<xsl:call-template name="header"/>

			<div class="container">
				<h1> <xsl:value-of select="@name"/></h1>
				<p class="text-right"><em>Last modified 	
				<xsl:value-of select="@lastmodified"/></em>.</p>

				<table class="table table-striped table-hover table-condensed">
					<thead>
						<tr class="info"><th>Link</th><th>Version</th><th>doc/src</th><th>Description</th><th>Bytes</th></tr>
					</thead>
					<tbody>
						<xsl:apply-templates select="resource">
							<xsl:sort select="@presentationname"/>
						</xsl:apply-templates>
					</tbody>
				</table>
			</div>


			<xsl:call-template name="footer"/>

		</body>
	</xsl:template>
	

	<!-- =========================================================== -->
	<!-- Row Resource Templates                                      -->
	<!-- =========================================================== -->

	<xsl:template match="resource">
		<tr>
			<td>
				<a href="{normalize-space(@uri)}"><xsl:value-of select="@presentationname"/></a>
				
			</td>
			<td><xsl:value-of select="@version"/></td>
			<td>
					<xsl:if test="documentation">
						<a href="{normalize-space(documentation)}">D</a>
					</xsl:if>
					<xsl:if test="source">
						<a href="{normalize-space(source)}">S</a>
					</xsl:if>
			</td>
			<td>
				<xsl:value-of select="description"/>
			</td>
			<td>
					<xsl:value-of select="size"/>
			</td>
		</tr>
		
	</xsl:template>
	
	<!--
	<xsl:template match="*">
	<tr>
	<td><xsl:value-of select="name()"/></td>
	<td><xsl:value-of select="."/></td>
	</tr>
	</xsl:template>
	-->
	<!--
	<xsl:template match="*">
	</xsl:template>
	-->

	<!-- =========================================================== -->
	<!-- Header Templates                                            -->
	<!-- =========================================================== -->

	<xsl:template name="header">
	  	<header id="masthead" class="blog-background overlay align-center align-bottom animated from-bottom animation-on" style="background-image: url(http://www.gvnix.org/assets/themes/gvnix-omega-multilanguage/images/default_bg.png)"> 
			
			<!-- ribbon github -->
			<a href="https://github.com/gvSIGAssociation/gvnix">
				<img style="position: absolute; top: 0; left: 0; border: 0; z-index: 30" src="https://camo.githubusercontent.com/567c3a48d796e2fc06ea80409cc9dd82bf714434/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f6c6566745f6461726b626c75655f3132313632312e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_left_darkblue_121621.png" /> 
			</a>			
	  
			<div class="inner">
			    <div class="container">
			    	<a class="brand light" href="http://www.gvnix.org" role="banner" itemprop="url">
			    		<img itemprop="logo" src="http://www.gvnix.org/assets/themes/gvnix-omega-multilanguage/images/logo-negativo-300.png" alt="gvNIX Logo" />
			    	</a>
			    	<h2 class="light" itemprop="description">
			    		<!-- Herramienta de desarrollo rápido de aplicaciones web -->
			    		Tool for rapid web application development 
			    	</h2>
			    	<h3 class="blog-description light">
			    		<!-- Mayor productividad gracias a sus componentes de alto valor funcional 
			    		para aplicaciones corporativas -->
			    		Higher productivity thanks to its components with high functional value for corporate applications
			    	</h3>
			    </div>
			</div>	    
		</header> 

	</xsl:template>

	<!-- ========================================================== -->
	<!-- Footer Templates                                           -->
	<!-- ========================================================== -->	

	<xsl:template name="footer">

		<a style="display: inline;" class="scrollup" href="#" rel="tooltip" data-original-title="" data-placement="left"></a>

		<footer id="footer" class="blog-background overlay text-center align-middle animated from-top" style="background-image: url(http://www.gvnix.org/assets/themes/gvnix-omega-multilanguage/images/default_bg.png)">

		    <div class="inner">
		        <div class="container">

		            <ul class="social-icons">
		            <li>
		                <a data-original-title="gvNIX on Twitter" href="http://twitter.com/gvnix" data-toggle="tooltip" title="" target="_blank">
		                    <i class="fa fa-twitter"></i>
		                </a>
		            </li>
		            
		            
		            <li>
		                <a data-original-title="gvNIX on StackOverflow" href="http://stackoverflow.com/questions/tagged/gvnix" data-toggle="tooltip" title="" target="_blank">
		                    <i class="fa fa-stack-overflow"></i>
		                </a>
		            </li>
		            <li>
		                <a data-original-title="gvNIX on LinkedIn" href="https://www.linkedin.com/groups/gvNIX-entorno-desarrollo-rápido-aplicaciones-3878961" data-toggle="tooltip" title="" target="_blank">
		                    <i class="fa fa-linkedin"></i>
		                </a>
		            </li>
		            
		            
		            <li>
		                <a data-original-title="gvNIX on Github" href="http://github.com/gvSIGAssociation/gvnix" data-toggle="tooltip" title="" target="_blank">
		                    <i class="fa fa-github"></i>
		                </a>
		            </li>
		        </ul>
		            <div>
		                <p class="rss-subscribe text-center">
		                  gvNIX © 2015 
		                </p>
		                <p class="rss-subscribe text-center">
		                <a data-original-title="Sitio web empresa líder del proyecto" href="http://www.disid.com" target="_blank" data-toggle="tooltip" title="">DISID Corporation</a> |
		                <a data-original-title="Sitio web gvNIX" href="http://www.gvnix.org" target="_blank" data-toggle="tooltip" title="">gvNIX</a> |
		                 <a data-original-title="Sitio web Spring Roo" href="http://projects.spring.io/spring-roo/" target="_blank" data-toggle="tooltip" title="">Spring Roo</a>
		                </p>
		            </div>
		        </div>
		    </div>
		</footer>

  
	</xsl:template>


	
</xsl:stylesheet>



