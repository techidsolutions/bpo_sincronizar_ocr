nohup java -jar Servicio/SincronizarRepoGrupoBCWS.jar > ws.out 2> ws.err < /dev/null &
nohup java -jar dist/SincronizarRepoGrupoBC.jar > b1.out 2> b1.err < /dev/null &
nohup java -jar ocr/SincronizarRepoGrupoBCOCR.jar > ocr.out 2> ocr.err < /dev/null &
