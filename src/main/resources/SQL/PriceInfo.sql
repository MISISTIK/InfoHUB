select s.STORE_NUM
    ,a.id
    ,a.ART_NUM
    ,s.ART_PURCH_PRICE
    ,s.ART_PRICE
from STORE_{1} s
join ARTICLES a on s.id = a.id
where ART_NUM = {0}