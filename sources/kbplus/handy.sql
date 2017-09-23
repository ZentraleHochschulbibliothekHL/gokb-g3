## Packages with content provider

select p.pkg_id, 
       p.pkg_name,
       o.org_name
from package as p, 
     org_role as orl, 
     org as o
where orl.or_pkg_fk = p.pkg_id 
  and orl.or_roletype_fk = 81 
  and orl.or_org_fk = o.org_id


## Content providers, by # packages 

select o.org_name,count(*) from org_role as orl, org as o where orl.or_roletype_fk = 81 and o.org_id = orl.or_org_fk group by o.org_name order by count(*) desc
