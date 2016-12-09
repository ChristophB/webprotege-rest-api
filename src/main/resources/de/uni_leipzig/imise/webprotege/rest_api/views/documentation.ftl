<#-- @ftlvariable name="" type="de.uni_leipzig.imise.webprotege.rest_api.views.DocumentationView" -->
<html>
	<head>
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	</head>
	
	<body>
		<div class="jumbotron text-center">
			<h2>Documentation of WebProt&#233;g&#233; REST-Interface</h2>
			<p>This page contains a list of all available RESTfull functions with respective URL and parameters.</p>
		</div>
    	
    	<div class="container">
    		<div class="row">
	    		<table class="table">
		    		<thead>
		    			<tr>
		   					<th>Path</th>
		   					<th></th>
		   					<th>Description</th>
		   				</tr>
		    		</thead>
		    		
		    		<tbody>
						
						<tr>
							<td colspan="2">/entity</td>
							<td>
								<p>Search for a single or multiple entities in multiple projects.</p>
								<ul>
									<li>see /project/{id}/entity</li>
									<li><b>ontologies:</b> List of comma separated ontology ids (default: all ontologies)</li>
								</ul>
							</td>	
						</tr>
						
						<tr>
							<td rowspan="5">/project</td>
						</tr>
						<tr>
							<td>/{id}</td>
							<td>Get full OWL document as RDF/XML.</td>
						</tr>
						
						<tr>
							<td>/{id}/entity</td>
							<td>
								<p>Search for single or multiple entities in project with respective id.</p>
								<ul>
									<li><b>type:</b> Entity, class or individual</li>
									<li><b>name:</b> Entity name</li>
									<li><b>match:</b> Match method for 'name' parameter: 'exact' or 'loose' (default: loose)</li>
									<li><b>property:</b> Name of a Property, the entity is annotated with</li>
									<li><b>value:</b> Value of the specified Property</li>
									<li><b>operator:</b> Logical operator to combine 'name' and 'property' (default: and)</li>
								</ul>
							</td>
						</tr>
						
						<tr>
							<td>/{id}/imports</td>
							<td>List all imports of the specified ontology.</td>
						</tr>
						
						<tr>
							<td>/{id}/reason</td>
							<td>
								<p>Search for individuals by reasoning in the specified project.</p>
								<ul>
									<li><b>ce:</b> Class expression (currently not working with short forms, use full IRIs instead)</li>
								</ul>
							</td>
						</tr>
						
						<tr>
							<td colspan="2">/projects</td>
							<td>List all available projects/ontologies with a short description and id.</td>
						</tr>
						
						<tr>
		    				<td colspan="2">/reason</td>
		    				<td>
		    					<p>Search for individuals by reasoning in multiple projects.</p>
		    	   				<ul>
		    	   					<li><b>ce:</b> Class expression (currently not working with short forms, use full IRIs instead)</li>
									<li><b>ontologies:</b> List of comma separated ontology ids (default: all ontologies)</li>
								</ul>
							</td>
						</tr>
						
		    		</tbody>
		    	</table>
		    </div>
	    </div>   
	</body>
</html>