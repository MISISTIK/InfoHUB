SELECT
vaj_cdmag
,to_char(vaj_dtrem,'dd.mm.yyyy') as vaj_dtrem
,sum(VAJ_MTVENTHT) as CA_NET
,sum(vaj_mtvente) as CA_TTC
,round(sum(vaj_qtvend),2) as QTY
,sum(round(VAJ_MTVENTHT-VAJ_MTACHAT, 2)) as MAR
FROM UAS035.MGVAJR
where vaj_dtrem between to_date('01.09.2018','dd.mm.yyyy') and to_date(sysdate)
group by vaj_cdmag, vaj_dtrem