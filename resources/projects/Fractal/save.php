<html>
<head>
<title>Addscore</title>
</head>
<body bgcolor=white>
<?
$sstr = "";
foreach ($_GET as $k => $v) {
	if ($sstr != "") $sstr.= "&";
	$sstr .= "$k=$v";
}

$file=fopen('saves.txt','a');
fwrite($file,"<option value='?$sstr'>$_GET[name]</option>\n");
fclose($file);

?>
</body>
</html>
