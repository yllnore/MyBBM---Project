-- ==============================================================================
-- SKEMA LENGKAP SUPABASE UNTUK MYBBM (APLIKASI ANDROID + LANDING PAGE WEBSITE)
-- Salin seluruh isi skrip ini, tempel ke Supabase SQL Editor, lalu klik RUN.
-- ==============================================================================

-- 1. TABEL JENIS BBM
CREATE TABLE IF NOT EXISTS public.jenis_bbm (
    id_bbm TEXT PRIMARY KEY,
    nama_bbm TEXT NOT NULL,
    jenis_bahan_bakar TEXT NOT NULL DEFAULT 'BENSIN',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2. TABEL HARGA BBM
CREATE TABLE IF NOT EXISTS public.harga_bbm (
    id_harga TEXT PRIMARY KEY,
    id_bbm TEXT NOT NULL REFERENCES public.jenis_bbm(id_bbm) ON DELETE CASCADE,
    wilayah TEXT NOT NULL,
    harga_per_liter NUMERIC(12, 2) NOT NULL,
    berlaku_mulai TIMESTAMPTZ NOT NULL DEFAULT now(),
    berlaku_hingga TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3. TABEL KENDARAAN (CATALOG STANDAR)
CREATE TABLE IF NOT EXISTS public.kendaraan (
    id_kendaraan TEXT PRIMARY KEY,
    nama_kendaraan TEXT NOT NULL,
    jenis_kendaraan TEXT NOT NULL, -- 'Mobil' atau 'Motor'
    konsumsi_km_per_liter NUMERIC(8, 2) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT true,
    user_id UUID,
    local_device_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

-- 4. TABEL RILIS APK (UNTUK DISTRIBUSI DI WEBSITE)
CREATE TABLE IF NOT EXISTS public.app_releases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_name TEXT NOT NULL,
    version_code INT NOT NULL,
    apk_url TEXT NOT NULL,
    file_size_mb NUMERIC(5, 2) NOT NULL DEFAULT 6.05,
    changelog TEXT,
    min_android_sdk INT NOT NULL DEFAULT 26,
    download_count BIGINT NOT NULL DEFAULT 0,
    is_latest BOOLEAN NOT NULL DEFAULT true,
    released_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 5. TABEL FEEDBACK / TESTIMONI WEBSITE
CREATE TABLE IF NOT EXISTS public.website_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    role TEXT,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT NOT NULL,
    is_approved BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ==============================================================================
-- KEAMANAN (ROW LEVEL SECURITY / RLS)
-- ==============================================================================

ALTER TABLE public.jenis_bbm ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.harga_bbm ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.kendaraan ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_releases ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.website_feedback ENABLE ROW LEVEL SECURITY;

-- Policy Read-Only Publik untuk Aplikasi & Website
DROP POLICY IF EXISTS "Allow public read jenis_bbm" ON public.jenis_bbm;
CREATE POLICY "Allow public read jenis_bbm" ON public.jenis_bbm FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow public read harga_bbm" ON public.harga_bbm;
CREATE POLICY "Allow public read harga_bbm" ON public.harga_bbm FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow public read kendaraan" ON public.kendaraan;
CREATE POLICY "Allow public read kendaraan" ON public.kendaraan FOR SELECT USING (deleted_at IS NULL);

DROP POLICY IF EXISTS "Allow public read app_releases" ON public.app_releases;
CREATE POLICY "Allow public read app_releases" ON public.app_releases FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow public read website_feedback" ON public.website_feedback;
CREATE POLICY "Allow public read website_feedback" ON public.website_feedback FOR SELECT USING (is_approved = true);

-- Policy Pengunjung Website Bisa Mengirim Ulasan
DROP POLICY IF EXISTS "Allow public insert website_feedback" ON public.website_feedback;
CREATE POLICY "Allow public insert website_feedback" ON public.website_feedback FOR INSERT WITH CHECK (name IS NOT NULL AND length(name) > 1 AND rating >= 1 AND rating <= 5);

-- RPC Function: Penghitung Download Otomatis & Aman
CREATE OR REPLACE FUNCTION public.increment_download_count(release_id UUID)
RETURNS BIGINT
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    new_count BIGINT;
BEGIN
    UPDATE public.app_releases
    SET download_count = download_count + 1
    WHERE id = release_id
    RETURNING download_count INTO new_count;
    RETURN new_count;
END;
$$;

-- ==============================================================================
-- SEED DATA AWAL (HARGA RESMI KALIMANTAN TIMUR & 10 KENDARAAN POPULER)
-- ==============================================================================

-- A. JENIS BBM
INSERT INTO public.jenis_bbm (id_bbm, nama_bbm, jenis_bahan_bakar) VALUES
('a1b2c3d4-0001-0001-0001-000000000001', 'Pertalite', 'BENSIN'),
('a1b2c3d4-0001-0001-0001-000000000002', 'Pertamax', 'BENSIN'),
('a1b2c3d4-0001-0001-0001-000000000003', 'Pertamax Turbo', 'BENSIN'),
('a1b2c3d4-0001-0001-0001-000000000004', 'Dexlite', 'DIESEL'),
('a1b2c3d4-0001-0001-0001-000000000005', 'Pertamina Dex', 'DIESEL'),
('a1b2c3d4-0001-0001-0001-000000000006', 'Biosolar', 'DIESEL')
ON CONFLICT (id_bbm) DO UPDATE SET nama_bbm = EXCLUDED.nama_bbm;

-- B. HARGA RESMI KALIMANTAN TIMUR
INSERT INTO public.harga_bbm (id_harga, id_bbm, wilayah, harga_per_liter) VALUES
('b1c2d3e4-0002-0002-0002-000000000001', 'a1b2c3d4-0001-0001-0001-000000000001', 'KALIMANTAN_TIMUR', 10000.00),
('b1c2d3e4-0002-0002-0002-000000000002', 'a1b2c3d4-0001-0001-0001-000000000002', 'KALIMANTAN_TIMUR', 12500.00),
('b1c2d3e4-0002-0002-0002-000000000003', 'a1b2c3d4-0001-0001-0001-000000000003', 'KALIMANTAN_TIMUR', 14000.00),
('b1c2d3e4-0002-0002-0002-000000000004', 'a1b2c3d4-0001-0001-0001-000000000004', 'KALIMANTAN_TIMUR', 13700.00),
('b1c2d3e4-0002-0002-0002-000000000005', 'a1b2c3d4-0001-0001-0001-000000000005', 'KALIMANTAN_TIMUR', 14950.00),
('b1c2d3e4-0002-0002-0002-000000000006', 'a1b2c3d4-0001-0001-0001-000000000006', 'KALIMANTAN_TIMUR', 6800.00)
ON CONFLICT (id_harga) DO UPDATE SET harga_per_liter = EXCLUDED.harga_per_liter;

-- C. 10 KENDARAAN POPULER INDONESIA
INSERT INTO public.kendaraan (id_kendaraan, nama_kendaraan, jenis_kendaraan, konsumsi_km_per_liter, is_default) VALUES
('seed-veh-01', 'HONDA BEAT 110 ESP', 'Motor', 60.00, true),
('seed-veh-02', 'HONDA SCOOPY 110', 'Motor', 59.00, true),
('seed-veh-03', 'YAMAHA FAZZIO', 'Motor', 55.00, true),
('seed-veh-04', 'HONDA VARIO 125', 'Motor', 51.70, true),
('seed-veh-05', 'YAMAHA NMAX 155', 'Motor', 40.00, true),
('seed-veh-06', 'HONDA PCX 160', 'Motor', 45.00, true),
('seed-veh-07', 'HONDA BRIO SATYA 1.2', 'Mobil', 18.00, true),
('seed-veh-08', 'TOYOTA AVANZA 1.3', 'Mobil', 14.00, true),
('seed-veh-09', 'MITSUBISHI XPANDER 1.5', 'Mobil', 13.50, true),
('seed-veh-10', 'TOYOTA KIJANG INNOVA REBORN', 'Mobil', 11.50, true)
ON CONFLICT (id_kendaraan) DO UPDATE SET konsumsi_km_per_liter = EXCLUDED.konsumsi_km_per_liter;

-- D. METADATA RILIS AWAL MYBBM v1.0
INSERT INTO public.app_releases (version_name, version_code, apk_url, file_size_mb, changelog, is_latest) VALUES
('v1.0.0', 1, 'MyBBM-v1.0.apk', 6.05, 'Rilis resmi pertama MyBBM: kalkulasi BBM rute akurat, pencarian SPBU terdekat, dan offline fallback ready.', true);
