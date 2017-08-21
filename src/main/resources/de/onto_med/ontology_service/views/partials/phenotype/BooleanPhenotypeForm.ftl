<form id="boolean-phenotype-form" action="${rootPath}/phenotype/create-restricted-phenotype" method="post" accept-charset="UTF-8" class="hidden">
	<input type="hidden" name="datatype" value="boolean">

	<#include "Id.ftl">
	<#include "Labels.ftl">
	<#include "SuperPhenotype.ftl">
    <#include "Definitions.ftl">
    <#include "Relations.ftl">

	<div class="form-group row">
	    <label class="control-label col-sm-2">Restriction*</label>

    	<div class="col-sm-10" id="datatype-specification">
    	    <#include "BooleanExpression.ftl">
		</div>
	</div>

	<div class="form-group">
		<input type="submit" class="btn btn-primary" value="Create Boolean Phenotype">
	</div>
</form>