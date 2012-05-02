
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<stripes:useActionBean id="bean" beanclass="com.acme.action.PageActionBean"/>
<stripes:layout-render name='../layout/main.jsp' pageTitle='Page List'>
	<stripes:layout-component name="adminBar">
		<stripes:layout-render name="../includes/_adminBar.jsp" bean="${bean}" links="create" />
	</stripes:layout-component>

	<stripes:layout-component name="content">
		<table id="PageList" class="tableList">
	<thead>
		<tr><th>name</th></tr>
	</thead>
	<tbody>
		<c:forEach items="${requestScope['list']}" var="item">
			<tr>
				<td>
					${item.name}
				</td>
				<td>
					<stripes:link href='/page/view?page=${item.id}'>View</stripes:link>&nbsp;|&nbsp;					<stripes:link href='/page/edit?page=${item.id}'>Edit</stripes:link>&nbsp;|&nbsp;<stripes:link class='deleteObject' href='/page/delete?page=${item.id}'>Delete</stripes:link>
				</td>
			</tr>
		</c:forEach>
	</tbody>
</table>
	</stripes:layout-component>
</stripes:layout-render>
