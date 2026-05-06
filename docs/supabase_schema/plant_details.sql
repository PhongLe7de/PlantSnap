create table public.plant_details (
  plant_gbif_id numeric not null,
  description text null,
  habitat text null,
  toxicity_human text null,
  care_temperature text null,
  care_light text null,
  care_water text null,
  care_humidity text null,
  care_soil text null,
  created_at timestamp with time zone not null default now(),
  toxicity_cat text null,
  toxicity_dog text null,
  foraging_notes text null,
  scientific_name text null,
  constraint plant_details_pkey primary key (plant_gbif_id)
) TABLESPACE pg_default;