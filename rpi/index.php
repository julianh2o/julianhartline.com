<?php

$key = "LcPo9u9J569RDNfQfzEA";
if (isset($_GET['register'])) {
    $f = fopen("host.txt", "w");
    fwrite($f,$_SERVER['REMOTE_ADDR'].":".$_GET['port']);
    fclose($f);
} else if (isset($_GET['send'])) {
    $f = fopen("host.txt", "r");
    $host = fread($f,100);
    print("host: $host");
    fclose($f);

    //$b64 = file_get_contents("php://input");

    if ($_GET["key"] != $key) return;

    $ch = curl_init();
    $url = "$host/send?action=".$_GET["action"];
    curl_setopt($ch,CURLOPT_URL,$url);
    curl_setopt($ch,CURLOPT_RETURNTRANSFER, 1);

    $data = curl_exec($ch);

    print $data;
}
