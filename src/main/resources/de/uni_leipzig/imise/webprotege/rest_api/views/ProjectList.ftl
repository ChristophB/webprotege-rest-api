<#-- @ftlvariable name="" type="de.uni_leipzig.imise.webprotege.rest_api.views.ProjectListView" -->
<#assign title = "Project List">
<#assign current = "Projects">

<html>
	<#include "Head.ftl">
	
	<body>
		<#include "Navbar.ftl">
		
		<div class="jumbotron text-center" style="padding: 10 0 10">
			<h2>WebProt&#233;g&#233; Project List</h2>
			<p>The following table contains all public readable projects/ontologies of the locally running WebProt&#233;g&#233; instance.</p>
		</div>
		
		<div class="container">
			<div class="row">
				<table class="table">
					<thead>
						<tr>
							<th>ID</th>
							<th>Name</th>
							<th>Description</th>
						</tr>
					</thead>
					
					<tbody>
						<#list projects as project>
							<tr>
								<td><a href="/project/${project.projectId}/overview">${project.projectId}</a></td>
								<td>${project.name}</td>
								<td>${project.description}</td>
							</tr>
						</#list>
					</tbody>
				</table>
			</div>
		</div>
	</body>
</html>