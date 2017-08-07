<#assign current = "Documentation">
<#assign title = "Documentation">
<#assign heading = "Documentation of the Ontology Service">
<#assign subHeading ="This page contains a list of all available RESTfull functions with respective URL and query/JSON parameters.">

<html>
	<#include "partials/Head.ftl">
	
	<body>
		<#include "partials/Navbar.ftl">
		<#include "partials/Heading.ftl">
		<#include "partials/Messages.ftl">
    	
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
							<td colspan="2"><a href="${rootPath}/clear_cache">/clear_cache</a></td>
							<td>Clears the ProjectManager cache to speed up the cognition of changes via WebProt&#233;g&#233;.</td>
						</tr>
						
						<tr>
							<td colspan="2">/entity</td>
							<td>
								<p>Search for a single or multiple entities in multiple projects.</p>
								<ul>
									<li><b>type:</b> Entity, class or individual</li>
									<li><b>name:</b> Entity name</li>
									<li><b>match:</b> Match method for 'name' parameter: 'exact' or 'loose' (default: loose)</li>
									<li><b>property:</b> Name of a Property, the entity is annotated with</li>
									<li><b>value:</b> Value of the specified Property</li>
									<li><b>operator:</b> Logical operator to combine 'name' and 'property' (default: and)</li>
									<li><b>ontologies:</b> List of comma separated ontology ids (default: all ontologies)</li>
								</ul>
							</td>	
						</tr>
						
						<tr>
							<td colspan="2">/entity-form</td>
							<td><p>Form-based user interface to search for entities in one or multiple projects.</p></td>
						</tr>
						
						<tr>
							<td rowspan="7">/phenotype</td>
						</tr>
						
						<tr>
							<td>/</td>
							<td>Overview page for phenotypes</td>
						</tr>
						
						<tr>
							<td>/all</td>
							<td>
								A List of all phenotypes and/or phenotype categories
								<ul>
									<li><b>type:</b> can be 'all', 'phenotype', 'category', 'integer', 'double', 'string', 'formula', 'expression' (default: all)</li>
								</ul>
							</td>
						</tr>
						
						<tr>
							<td>/create</td>
							<td>
								Creates a phenotype with provided data. <i>/simple-phenotype-form</i> and <i>/composit-phenotype-form</i> send their data to this endpoint.<br>
								All parameters in the following list, which end with '[]' are handled as lists and thus can be provided multiple times. Their order may be important to map for example language to label.
								<ul>
									<li><b>id:</b> Unique identifier</li>
									<li><b>label[]:</b> Label</li>
									<li><b>label-language[]:</b> Label language</li>
									<li><b>has-super-phenotype:</b> Signals if the phenotype has a super phenotype. Use 'true' as value.</li>
									<li><b>super-phenotype:</b> The ID of an existing phenotype, which will be used as super phenotype.</li>
									<li><b>category:</b> Category ID in which the phenotype will be placed</li>
									<li><b>new-category:</b> A new Category's ID</li>
									<li><b>definition[]:</b> Textual definition</li>
									<li><b>definition-language[]:</b> Definition language</li>
									<li><b>datatype:</b> one of 'integer', 'double', 'string', 'formula', 'expression' (required)</li>
									<li><b>ucum:</b> Unit of a integer/double/formula phenotype as UCUM</li>
									<li><b>range-min[]:</b> Minimal value for sub phenotype range</li>
									<li><b>range-min-operator[]:</b> Operator for range-min[] ('=', '&ge;', '>')</li>
									<li><b>range-max[]:</b> Maximum value for sub phenotype range</li>
									<li><b>range-max-operator[]:</b> Operator for range-max[] ('<', '&le;', '=')</li>
									<li><b>enum-value[]:</b> Enumeration value for string phenotypes</li>
									<li><b>enum-label[]:</b> Label for enum[value] or a numeric range, specified by range-x[] and range-x-operator[]</li>
									<li><b>formula:</b> A mathematical formula which may contain other numerical phenotypes.</li>
									<li><b>expression:</b> A logical expression which may contain other phenotypes and mathematic symbols.</li>
									<li><b>boolean-true-label:</b> Label for phenotypes, where the expression evaluates to true.</li>
									<li><b>boolean-false-label:</b> Label for phenotypes, where the expression evaluates to false.</li>
									<li><b>relation[]:</b> IRI referencing other ontological entities.</li>
								</ul>	
							</td>
						</tr>
						
						<tr>
							<td>/composit-phenotype-form</td>
							<td>A form to create a <b>composit</b> phenotype</td>
						</tr>
						
						<tr>
							<td>/decision-tree</td>
							<td>
								Generates a decision tree for the specified phenotype.
								<ul><li><b>phenotype:</b> The phenotype identifier for which a decision tree will be generated.</li></ul>
							</td>
						</tr>
						
						<tr>
							<td>/simple-phenotype-form</td>
							<td>A form to create a <b>simple</b> phenotype</td>
						</tr>
						
						<tr>
							<td rowspan="4">/project/{id}</td>
						</tr>
						
						<tr>
							<td>/</td>
							<td>Get full OWL document as RDF/XML.</td>
						</tr>
						
						<tr>
							<td>/classify</td>
							<td>
								<p>
									Creates an individual from JSON and returns its infered classes. (only available via JSON request)<br>
									JSON template:
									<small>
										<pre>
[ { "types": [ "http://onto-med.de/auxology#patient" ],
    "properties": [
      { "iri":       "http://onto-med.de/auxology#bmi_sds",
        "className": "float",
        "values":    [ "-1.5f" ] }
    ] }, ... 
]</pre>
    								</small>
								</p>
							</td>
						</tr>
						
						<tr>
							<td>/overview</td>
							<td><p>Short overview page for the specified project.</p></td>
						</tr>
						
						<tr>
							<td colspan="2">/projects</td>
							<td>List all available projects/ontologies with a short description and id.</td>
						</tr>
						
						<tr>
		    				<td colspan="2">/reason</td>
		    				<td>
		    					<p>Search for individuals by reasoning in one or multiple projects.</p>
		    	   				<ul>
		    	   					<li><b>ce:</b> Class expression</li>
									<li><b>ontologies:</b> List of comma separated ontology ids (default: all ontologies)</li>
								</ul>
							</td>
						</tr>
						
						<tr>
							<td colspan="2">/reason-form</td>
							<td><p>Form-based user interface to reason in one or multiple projects.</p></td>
						</tr>
						
		    		</tbody>
		    	</table>
		    </div>
	    </div>
	    
	    <#include "partials/Footer.ftl">
	</body>
</html>