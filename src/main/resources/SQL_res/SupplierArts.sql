select
ART_SUPPLIER
,id
,SEGMENT
,ART_NUM
,ART_NAME
,STATUS
from ARTICLES
where ART_SUPPLIER = {0}
