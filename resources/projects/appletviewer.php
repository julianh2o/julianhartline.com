<html>
<head>
<?
echo "<title>$_GET[title]</title>";
?>
<style type=text/css>
body {
	margin: 0px;
}
</style>

</head>

<body>
<?
echo "<applet codebase='$_GET[codebase]' code='$_GET[class]' width=$_GET[width] height=$_GET[height]></applet>";
?>
</body>

</html>
