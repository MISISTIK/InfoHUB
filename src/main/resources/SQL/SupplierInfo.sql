select s.*
from SUPPLIERS s
join ARTICLES a on s.SUPPLIER_NUM = a.ART_SUPPLIER
where a.ART_NUM = {0}
