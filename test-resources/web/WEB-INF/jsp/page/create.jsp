
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<stripes:useActionBean id="bean" beanclass="com.acme.action.PageActionBean"/>
<stripes:layout-render name='../layout/main.jsp' pageTitle='Page create'>
	<stripes:layout-component name="adminBar">
		<stripes:layout-render name="../includes/_adminBar.jsp" bean="${bean}" links="list" />
	</stripes:layout-component>

	<stripes:layout-component name="content">
		
	<stripes:form beanclass="com.acme.action.PageActionBean">
		<div class='fieldRow'>
	<div class='fieldLabel'>
		<label for='page.name'>name</label>
	</div>
	<div class='fieldInput'>
		<input type="text" value="${bean.page.name}" name="page.name" />
	</div>
</div>

<stripes:hidden name="page" value="${bean.page.id}" />
		<stripes:submit name="save" value="Save" />
	</stripes:form>

	</stripes:layout-component>
</stripes:layout-render>
