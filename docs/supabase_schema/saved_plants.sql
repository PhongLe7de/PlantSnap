create table public.saved_plants (
  id uuid not null default gen_random_uuid (),
  user_id uuid not null default gen_random_uuid (),
  plant_gbif_id numeric not null,
  original_scan_id uuid not null default gen_random_uuid (),
  nickname text not null,
  is_archived boolean not null default false,
  last_watered_at timestamp with time zone null,
  created_at timestamp with time zone not null default now(),
  is_favourite boolean not null default false,
  image_url text null,
  constraint saved_plants_pkey primary key (id),
  constraint saved_plants_original_scan_id_fkey foreign KEY (original_scan_id) references scans (id),
  constraint saved_plants_plant_gbif_id_fkey foreign KEY (plant_gbif_id) references plant_details (plant_gbif_id),
  constraint saved_plants_user_id_fkey foreign KEY (user_id) references profiles (user_id) on update CASCADE on delete CASCADE
) TABLESPACE pg_default;