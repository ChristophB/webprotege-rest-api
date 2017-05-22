<#-- @ftlvariable name="" type="de.onto_med.webprotege_rest_api.views.ProjectTaxonomyView" -->
<#assign title = project.name?html>
<#assign current = "Projects">
<#assign current_submenu = "Taxonomy">
<#setting url_escaping_charset="UTF-8">

<#macro class_node node>
	<span class="class-node" iri="${node.iri}">
		${node.name} <#if node.individuals?? && (node.individuals > 0)>[${node.individuals}]</#if>
	</span>
	
	
	<#if node.children??>
		<ul>
			<#list node.children as child>
				<li><@class_node child /></li>
			</#list>
		</ul>
	</#if>
</#macro>

<html>
	<#include "Head.ftl">
	
	<body>
		<#include "Navbar.ftl">
		
		<div class="jumbotron text-center" style="padding: 10 0 10">
			<h2>${project.name?html}</h2>
			<p>${project.description}</p>
		</div>
		
		<div class="container">
			<#include "ProjectLinks.ftl">
			
			<div class="row">
				<div id="taxonomy-tree" class="well col-md-5">
					<ul>
						<li class="jstree-open"><@class_node taxonomy /></li>
					</ul>
				</div>
				
				<pre id="class-description" class="col-md-7"></pre>
			</div>
		</div>
		
		<script type="text/javascript">
			$('#taxonomy-tree').jstree();
		
			$('#taxonomy-tree').bind('select_node.jstree', function(e, selected) {
				var data = {
					ontologies: '${project.projectId}',
					iri:         jQuery.parseHTML(selected.node.text)[0].getAttribute('iri'),
					match:       'exact'
				};
				
				$.getJSON('${rootPath}/entity', data, function(json) {
					$('#class-description').html(JSON.stringify(json, null, 2));
				}, 'application/json');
			});
		</script>
	
		<#include "Footer.ftl">
	</body>
</html>