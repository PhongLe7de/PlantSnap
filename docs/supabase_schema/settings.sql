create table public.settings (
  user_id uuid not null,
  theme text not null default 'SYSTEM'::text,
  temperature_unit text not null default 'CELSIUS'::text,
  language text not null default 'en'::text,
  notifications_enabled boolean not null default true,
  plant_care_reminders boolean not null default true,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now(),
  constraint settings_pkey primary key (user_id),
  constraint settings_user_id_fkey foreign KEY (user_id) references auth.users (id) on delete CASCADE,
  constraint settings_theme_check check (
    (
      theme = any (
        array['LIGHT'::text, 'DARK'::text, 'SYSTEM'::text]
      )
    )
  ),
  constraint settings_unit_check check (
    (
      temperature_unit = any (array['CELSIUS'::text, 'FAHRENHEIT'::text])
    )
  )
) TABLESPACE pg_default;

create trigger settings_updated_at BEFORE
update on settings for EACH row
execute FUNCTION update_settings_timestamp ();