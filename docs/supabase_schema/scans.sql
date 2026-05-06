create table public.scans (
  id uuid not null default gen_random_uuid (),
  device_id text null,
  plant_gbif_id numeric null,
  identification_score double precision null,
  detected_organ text null,
  all_results jsonb null,
  latitude double precision null,
  longitude double precision null,
  scanned_at timestamp with time zone not null default now(),
  user_id uuid not null,
  constraint id_pkey primary key (id),
  constraint scans_user_id_fkey foreign KEY (user_id) references profiles (user_id) on delete CASCADE
) TABLESPACE pg_default;