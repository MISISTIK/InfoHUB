SELECT
CAST(s.store_code as signed) as store_code
,a.article_number
,CAST(p.pallet_number as CHAR) as pallet_number
,SUM(pa.article_quantity) as qty_sum
from pallet p
JOIN store s ON p.id_store = s.id_store
JOIN pallet_article pa ON p.id_pallet = pa.id_pallet
JOIN article a ON pa.id_article = a.id_article
where p.is_confirmed = 1
and p.id_pallet_status = '2'
and a.article_number <> 0
GROUP BY a.article_number