<?
$imagefolder = "images";
$idfile = "$imagefolder/id.txt";
if (file_exists($idfile)) {
	$c = file($idfile);
	$c = $c[0];
	if ($c == "") $c = 0;
} else {
	$c = 0;
}
$f = fopen("$imagefolder/$c.gif","w");
$data = file_get_contents("php://input");
fwrite($f,$data);
fclose($f);

echo $c;

$f2 = fopen($idfile,"w");
fwrite($f2,$c+1);
fclose($f2);
?>
