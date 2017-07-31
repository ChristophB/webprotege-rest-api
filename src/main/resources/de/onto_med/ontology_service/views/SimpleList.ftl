<#-- @ftlvariable name="" type="de.onto_med.webprotege_rest_api.views.SimpleListView" -->
<#assign title = "Resultset">
<#assign current = "Projects">

<html>
	<#include "partials/Head.ftl">
	
	<body>
		<#include "partials/Navbar.ftl">
		
		<div class="jumbotron text-center" style="padding: 10 0 10">
			<h2>Resultset</h2>
			<p>Set of ${column?html}.</p>
		</div>
		
		<div class="container">
			<div class="row">
				<table class="table">
					<thead>
						<tr>
							<th>${column?html}</th>
						</tr>
					</thead>
					
					<tbody>
						<#list resultset as result>
							<tr>
								<td>${result?html}</td>
							</tr>
						</#list>
					</tbody>
				</table>
			</div>
		</div>
		
		<#include "partials/Footer.ftl">
	</body>
</html>