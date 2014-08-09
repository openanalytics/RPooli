<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ramlUrl" value="${baseUri}/raml/ui/index.html?raml=${baseUri}/raml/api_v1.raml" />
<html>
<head>
<meta http-equiv="refresh" content="0; url=${ramlUrl}">
</head>
<body>
  Redirecting you to <a href="${ramlUrl}">the RAML console</a>.
</body>
</html>