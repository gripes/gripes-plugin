
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<stripes:useActionBean id="bean" beanclass="com.acme.action.PageActionBean"/>
<stripes:layout-render name='../layout/main.jsp' pageTitle='Page View'>
	<stripes:layout-component name="adminBar">
		<stripes:layout-render name="../includes/_adminBar.jsp" bean="${bean}" links="list,create" />
	</stripes:layout-component>

	<stripes:layout-component name="content">
		<div class='fieldRow'>
	<span class='fieldLabel'>name</span>
	<span class='fieldValue'>${bean.page.name}</span>
</div>

	</stripes:layout-component>
</stripes:layout-render>
