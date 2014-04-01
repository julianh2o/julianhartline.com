<?

$post_body = file_get_contents('php://input');
$json = json_decode($post_body);
$status = (trim($json->{"value"}) === "1") ? "true" : "false";
$data = $json->{"value"};
$log = "[".date("Y-m-d H:i:s")."] ".$data."\n";
file_put_contents("./millbraemanor/log.txt",$log,FILE_APPEND);

if ($data == "led on" || $data == "led off") {
	file_put_contents("./millbraemanor/status.txt",$data);
}

?>
