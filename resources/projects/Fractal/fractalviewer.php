<html>
<head>
<title>Fractal</title>
<style type=text/css>
body {
	margin: 0px;
	background-color: black;
	color: white;
	font-size: 14px;
}

a:link { 
	color: #3333ff;
	text-decoration: none;
}

a:visited { 
	color: #6699cc;
	text-decoration: none;
}

a:hover { 
	color: #9933ff;
	text-decoration: underline;
}

a:active { 
	color: #009900;
	text-decoration: none;
}

</style>
<script type=text/javascript>
window.onload = function() {
	setInterval("update()",1000);
}

function update() {
	var loc = window.location.href;
	var spl = loc.split("?");
	loc = spl[0];
	document.getElementById("fractalurl").href = loc+document.getElementById("fractal").getFractalURL();
}
</script>
</head>

<body>
<center>
<select id='saves' onchange='location.href=this.options[this.selectedIndex].value;'>
<option value='index.php'>Saves</option>
<?
$save=file('saves.txt');
foreach ($save as $echo) {
    echo "$echo";
}
?>
</select>
<br>
<a id=fractalurl>Link to this Fractal</a>
</center>
<?
if (isset($_GET['mac'])) {
	echo "<applet id='fractal' width='1050' height='600' code='FractalMac.class'>";
} else {
	echo "<applet id='fractal' width='1050' height='600' code='Fractal.class'>";
}
$name=$_GET['name'];
$it=$_GET['iter'];
$dA=$_GET['dang'];
$dL=$_GET['dlen'];
$il=$_GET['ilen'];
$ib=$_GET['ibran'];
$br=$_GET['branch'];
$off=$_GET['offset'];
$col=$_GET['color'];
$del=$_GET['delay'];
$rot=$_GET['rotate'];
if ($name!=null) {echo "<param name='name' value='$name'>";} else {echo "<param name='name' value=''>";}
if ($it!=null) {echo "<param name='iter' value='$it'>";} else {echo "<param name='iter' value='10'>";}
if ($dA!=null) {echo "<param name='dang' value='$dA'>";} else {echo "<param name='dang' value='45'>";}
if ($dL!=null) {echo "<param name='dlen' value='$dL'>";} else {echo "<param name='dlen' value='.75'>";}
if ($il!=null) {echo "<param name='ilen' value='$il'>";} else {echo "<param name='ilen' value='50'>";}
if ($ib!=null) {echo "<param name='ibran' value='$ib'>";} else {echo "<param name='ibran' value='4'>";}
if ($br!=null) {echo "<param name='branch' value='$br'>";} else {echo "<param name='branch' value='2'>";}
if ($off!=null) {echo "<param name='offset' value='$off'>";} else {echo "<param name='offset' value='0'>";}
if ($col!=null) {echo "<param name='color' value='$col'>";} else {echo "<param name='color' value='(0,0,255)'>";}
if ($del!=null) {echo "<param name='delay' value='$del'>";} else {echo "<param name='delay' value='100'>";}
if ($rot!=null) {echo "<param name='rotate' value='$rot'>";} else {echo "<param name='rotate' value='0'>";}
?>
</applet>

</body>

</html>
