<!doctype html>
<html lang="en" class="no-js">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>
        <g:layoutTitle default="GOKB G3"/>
    </title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>

    <asset:stylesheet src="application.css"/>

    <g:layoutHead/>
</head>
<body>

  <div class="navbar navbar-default navbar-fixed-top">

    <div class="container-fluid">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="#">GOKB G3</a>
      </div>

      <div class="collapse navbar-collapse pull-right">
        <ul class="nav navbar-nav">
          <sec:ifLoggedIn>
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown"><sec:username/><b class="caret"></b></a>
              <ul class="dropdown-menu pull-right">
                <li class="divider"></li>
                <li><g:link controller="home" action="logout">Logout</g:link></li>
              </ul>
            </li>
          </sec:ifLoggedIn>
          <sec:ifNotLoggedIn>
            <li class="${controllerName=='home' && actionName=='login' ? 'active' : ''}"><g:link controller="home" action="login">Login</g:link></li>
          </sec:ifNotLoggedIn>
        </ul>
      </div>
    </div>
  </div>

</body>
</html>
