create table public.profiles (
  user_id uuid not null default auth.uid (),
  created_at timestamp with time zone not null default now(),
  onboarding_completed boolean null default false,
  pet_type text null,
  plant_interests text[] null,
  experience_level text null,
  constraint profiles_pkey primary key (user_id),
  constraint profiles_user_id_fkey foreign KEY (user_id) references auth.users (id) on delete CASCADE
) TABLESPACE pg_default;

create trigger on_profile_created
after INSERT on profiles for EACH row
execute FUNCTION create_default_settings ();