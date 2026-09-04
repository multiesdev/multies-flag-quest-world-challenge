import os
import math

AUTH_DIR = "app/src/main/assets/flags/authentic"
FAKE_DIR = "app/src/main/assets/flags/fake"

os.makedirs(AUTH_DIR, exist_ok=True)
os.makedirs(FAKE_DIR, exist_ok=True)

def save_svg(filepath, content):
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content.strip())

def make_star(cx, cy, r_out, r_in, fill_color, points=5, rot_deg=0):
    pts = []
    angle_step = math.pi / points
    start_angle = math.radians(rot_deg) - math.pi / 2
    for i in range(2 * points):
        r = r_out if i % 2 == 0 else r_in
        angle = start_angle + i * angle_step
        x = cx + r * math.cos(angle)
        y = cy + r * math.sin(angle)
        pts.append(f"{x:.2f},{y:.2f}")
    return f'<polygon points="{" ".join(pts)}" fill="{fill_color}" />'

def generate_all():
    # 1. US - United States
    # Authentic: 50 stars, 13 stripes
    us_stripes = "".join([f'<rect y="{i * 38.461:.2f}" width="950" height="38.46" fill="{"#B22234" if i % 2 == 0 else "#FFFFFF"}"/>' for i in range(13)])
    canton_us = '<rect width="380" height="269.23" fill="#3C3B6E"/>'
    
    # 50 stars grid (9 rows of 6 and 5)
    stars_auth = []
    for r in range(9):
        cols = 6 if r % 2 == 0 else 5
        x_offset = 31.6 if r % 2 == 0 else 63.3
        y = 26.9 + r * 26.9
        for c in range(cols):
            x = x_offset + c * 63.3
            stars_auth.append(make_star(x, y, 11, 4.4, "#FFFFFF"))
    
    us_svg_auth = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 950 500">{us_stripes}{canton_us}{"".join(stars_auth)}</svg>'
    
    # Fake US: 49 stars
    stars_fake = []
    for r in range(9):
        cols = 6 if r in [0, 2, 6, 8] else 5
        x_offset = 31.6 if cols == 6 else 63.3
        y = 26.9 + r * 26.9
        for c in range(cols):
            x = x_offset + c * 63.3
            stars_fake.append(make_star(x, y, 11, 4.4, "#FFFFFF"))
    us_svg_fake = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 950 500">{us_stripes}{canton_us}{"".join(stars_fake)}</svg>'
    
    save_svg(f"{AUTH_DIR}/flag_us.svg", us_svg_auth)
    save_svg(f"{FAKE_DIR}/flag_us_fake.svg", us_svg_fake)

    # 2. CA - Canada
    # Authentic: 11-point maple leaf
    ca_base = '<rect width="150" height="300" fill="#FF0000"/><rect x="150" width="300" height="300" fill="#FFFFFF"/><rect x="450" width="150" height="300" fill="#FF0000"/>'
    leaf_11 = '<path d="M 300 240 L 290 230 L 293 205 L 275 215 L 260 195 L 268 185 L 235 165 L 250 155 L 240 140 L 275 145 L 285 110 L 300 80 L 315 110 L 325 145 L 360 140 L 350 155 L 365 165 L 332 185 L 340 195 L 325 215 L 307 205 L 310 230 Z" fill="#FF0000"/>'
    ca_svg_auth = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 300">{ca_base}{leaf_11}</svg>'
    
    # Fake Canada: 13-point maple leaf
    leaf_13 = '<path d="M 300 240 L 290 230 L 293 205 L 280 212 L 275 220 L 260 195 L 268 185 L 235 165 L 250 155 L 240 140 L 260 142 L 275 145 L 285 110 L 300 80 L 315 110 L 325 145 L 340 142 L 360 140 L 350 155 L 365 165 L 332 185 L 340 195 L 325 220 L 320 212 L 307 205 L 310 230 Z" fill="#FF0000"/>'
    ca_svg_fake = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 300">{ca_base}{leaf_13}</svg>'
    save_svg(f"{AUTH_DIR}/flag_ca.svg", ca_svg_auth)
    save_svg(f"{FAKE_DIR}/flag_ca_fake.svg", ca_svg_fake)

    # 3. BR - Brazil
    # Authentic: Band curves upward
    br_bg = '<rect width="1000" height="700" fill="#009B3A"/><polygon points="500,119 893,350 500,581 107,350" fill="#FEDF00"/><circle cx="500" cy="350" r="245" fill="#002776"/>'
    br_band_auth = '<path d="M 260 380 Q 500 270 740 380 L 735 410 Q 500 300 265 410 Z" fill="#FFFFFF"/><text x="500" y="340" font-family="sans-serif" font-weight="bold" font-size="20" fill="#009B3A" text-anchor="middle">ORDEM E PROGRESSO</text>'
    br_stars = ''.join([make_star(500 + x, 350 + y, 6, 2.5, "#FFFFFF") for x, y in [(-80, 80), (40, 90), (100, 70), (0, 110), (-120, 100), (80, 120), (-20, 50), (140, 90)]])
    br_svg_auth = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 700">{br_bg}{br_band_auth}{br_stars}</svg>'
    
    # Fake Brazil: Band curves downward
    br_band_fake = '<path d="M 260 320 Q 500 430 740 320 L 735 290 Q 500 400 265 290 Z" fill="#FFFFFF"/><text x="500" y="365" font-family="sans-serif" font-weight="bold" font-size="20" fill="#009B3A" text-anchor="middle">ORDEM E PROGRESSO</text>'
    br_svg_fake = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 700">{br_bg}{br_band_fake}{br_stars}</svg>'
    save_svg(f"{AUTH_DIR}/flag_br.svg", br_svg_auth)
    save_svg(f"{FAKE_DIR}/flag_br_fake.svg", br_svg_fake)

    # 4. ES - Spain
    # Authentic: Coat of arms at x=250 (1/3 hoist)
    es_stripes = '<rect width="900" height="150" fill="#AA152B"/><rect y="150" width="900" height="300" fill="#F1BF00"/><rect y="450" width="900" height="150" fill="#AA152B"/>'
    coat_vector = '''<g transform="translate({x}, 225)">
        <rect x="0" y="0" width="100" height="120" rx="10" fill="#AA152B" stroke="#F1BF00" stroke-width="4"/>
        <rect x="50" y="0" width="50" height="60" fill="#FFFFFF"/>
        <rect x="0" y="60" width="50" height="60" fill="#F1BF00"/>
        <path d="M 20 -20 L 80 -20 L 90 -40 L 50 -30 L 10 -40 Z" fill="#F1BF00"/>
        <circle cx="50" cy="-35" r="8" fill="#AA152B"/>
        <rect x="-30" y="-10" width="15" height="130" fill="#C0C0C0"/>
        <rect x="115" y="-10" width="15" height="130" fill="#C0C0C0"/>
    </g>'''
    es_svg_auth = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{es_stripes}{coat_vector.format(x=225)}</svg>'
    es_svg_fake = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{es_stripes}{coat_vector.format(x=400)}</svg>'
    save_svg(f"{AUTH_DIR}/flag_es.svg", es_svg_auth)
    save_svg(f"{FAKE_DIR}/flag_es_fake.svg", es_svg_fake)

    # 5. IT - Italy
    it_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="300" height="600" fill="#009246"/><rect x="300" width="300" height="600" fill="#FFFFFF"/><rect x="600" width="300" height="600" fill="#CE2B37"/></svg>'
    it_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="300" height="600" fill="#CE2B37"/><rect x="300" width="300" height="600" fill="#FFFFFF"/><rect x="600" width="300" height="600" fill="#009246"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_it.svg", it_auth)
    save_svg(f"{FAKE_DIR}/flag_it_fake.svg", it_fake)

    # 6. DE - Germany
    de_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 600"><rect width="1000" height="200" fill="#000000"/><rect y="200" width="1000" height="200" fill="#DD0000"/><rect y="400" width="1000" height="200" fill="#FFCC00"/></svg>'
    de_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 600"><rect width="1000" height="200" fill="#000000"/><rect y="200" width="1000" height="200" fill="#FFCC00"/><rect y="400" width="1000" height="200" fill="#DD0000"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_de.svg", de_auth)
    save_svg(f"{FAKE_DIR}/flag_de_fake.svg", de_fake)

    # 7. FR - France
    fr_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="300" height="600" fill="#002654"/><rect x="300" width="300" height="600" fill="#FFFFFF"/><rect x="600" width="300" height="600" fill="#ED2939"/></svg>'
    fr_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="300" height="600" fill="#ED2939"/><rect x="300" width="300" height="600" fill="#FFFFFF"/><rect x="600" width="300" height="600" fill="#002654"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_fr.svg", fr_auth)
    save_svg(f"{FAKE_DIR}/flag_fr_fake.svg", fr_fake)

    # 8. IN - India
    # 24 spokes vs 18 spokes
    in_bg = '<rect width="900" height="200" fill="#FF9933"/><rect y="200" width="900" height="200" fill="#FFFFFF"/><rect y="400" width="900" height="200" fill="#138808"/>'
    def make_chakra(spokes):
        lines = []
        for i in range(spokes):
            angle = math.radians(i * (360 / spokes))
            x2 = 450 + 85 * math.cos(angle)
            y2 = 300 + 85 * math.sin(angle)
            lines.append(f'<line x1="450" y1="300" x2="{x2:.2f}" y2="{y2:.2f}" stroke="#000080" stroke-width="4"/>')
        return f'<circle cx="450" cy="300" r="85" fill="none" stroke="#000080" stroke-width="8"/><circle cx="450" cy="300" r="16" fill="#000080"/>{"".join(lines)}'
    
    in_svg_auth = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{in_bg}{make_chakra(24)}</svg>'
    in_svg_fake = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{in_bg}{make_chakra(18)}</svg>'
    save_svg(f"{AUTH_DIR}/flag_in.svg", in_svg_auth)
    save_svg(f"{FAKE_DIR}/flag_in_fake.svg", in_svg_fake)

    # 9. IE - Ireland
    ie_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 600"><rect width="400" height="600" fill="#169B62"/><rect x="400" width="400" height="600" fill="#FFFFFF"/><rect x="800" width="400" height="600" fill="#FF883E"/></svg>'
    ie_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 600"><rect width="400" height="600" fill="#FF883E"/><rect x="400" width="400" height="600" fill="#FFFFFF"/><rect x="800" width="400" height="600" fill="#169B62"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_ie.svg", ie_auth)
    save_svg(f"{FAKE_DIR}/flag_ie_fake.svg", ie_fake)

    # 10. SE - Sweden
    se_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1600 1000"><rect width="1600" height="1000" fill="#006AA7"/><rect x="500" width="200" height="1000" fill="#FECC00"/><rect y="400" width="1600" height="200" fill="#FECC00"/></svg>'
    se_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1600 1000"><rect width="1600" height="1000" fill="#006AA7"/><rect x="700" width="200" height="1000" fill="#FECC00"/><rect y="400" width="1600" height="200" fill="#FECC00"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_se.svg", se_auth)
    save_svg(f"{FAKE_DIR}/flag_se_fake.svg", se_fake)

    # 11. NO - Norway
    no_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1100 800"><rect width="1100" height="800" fill="#BA0C2F"/><rect x="300" width="200" height="800" fill="#FFFFFF"/><rect y="300" width="1100" height="200" fill="#FFFFFF"/><rect x="350" width="100" height="800" fill="#00205B"/><rect y="350" width="1100" height="100" fill="#00205B"/></svg>'
    no_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1100 800"><rect width="1100" height="800" fill="#BA0C2F"/><rect x="350" width="100" height="800" fill="#00205B"/><rect y="350" width="1100" height="100" fill="#00205B"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_no.svg", no_auth)
    save_svg(f"{FAKE_DIR}/flag_no_fake.svg", no_fake)

    # 12. DK - Denmark
    dk_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1480 1120"><rect width="1480" height="1120" fill="#C8102E"/><rect x="480" width="160" height="1120" fill="#FFFFFF"/><rect y="480" width="1480" height="160" fill="#FFFFFF"/></svg>'
    dk_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1480 1120"><rect width="1480" height="1120" fill="#FFFFFF"/><rect x="480" width="160" height="1120" fill="#C8102E"/><rect y="480" width="1480" height="160" fill="#C8102E"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_dk.svg", dk_auth)
    save_svg(f"{FAKE_DIR}/flag_dk_fake.svg", dk_fake)

    # 13. FI - Finland
    fi_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1800 1100"><rect width="1800" height="1100" fill="#FFFFFF"/><rect x="500" width="300" height="1100" fill="#002F6C"/><rect y="400" width="1800" height="300" fill="#002F6C"/></svg>'
    fi_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1800 1100"><rect width="1800" height="1100" fill="#FFFFFF"/><rect x="750" width="300" height="1100" fill="#002F6C"/><rect y="400" width="1800" height="300" fill="#002F6C"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_fi.svg", fi_auth)
    save_svg(f"{FAKE_DIR}/flag_fi_fake.svg", fi_fake)

    # 14. AT - Austria
    at_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="900" height="200" fill="#ED2939"/><rect y="200" width="900" height="200" fill="#FFFFFF"/><rect y="400" width="900" height="200" fill="#ED2939"/></svg>'
    at_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="900" height="200" fill="#FFFFFF"/><rect y="200" width="900" height="200" fill="#ED2939"/><rect y="400" width="900" height="200" fill="#FFFFFF"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_at.svg", at_auth)
    save_svg(f"{FAKE_DIR}/flag_at_fake.svg", at_fake)

    # 15. NL - Netherlands
    nl_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="900" height="200" fill="#AE1C28"/><rect y="200" width="900" height="200" fill="#FFFFFF"/><rect y="400" width="900" height="200" fill="#21468B"/></svg>'
    nl_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="900" height="200" fill="#21468B"/><rect y="200" width="900" height="200" fill="#FFFFFF"/><rect y="400" width="900" height="200" fill="#AE1C28"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_nl.svg", nl_auth)
    save_svg(f"{FAKE_DIR}/flag_nl_fake.svg", nl_fake)

    # 16. PL - Poland
    pl_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 750"><rect width="1200" height="375" fill="#FFFFFF"/><rect y="375" width="1200" height="375" fill="#DC143C"/></svg>'
    pl_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 750"><rect width="1200" height="375" fill="#DC143C"/><rect y="375" width="1200" height="375" fill="#FFFFFF"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_pl.svg", pl_auth)
    save_svg(f"{FAKE_DIR}/flag_pl_fake.svg", pl_fake)

    # 17. TR - Turkey
    # Star outside horns vs inside horns
    tr_bg = '<rect width="900" height="600" fill="#E30A17"/><circle cx="350" cy="300" r="150" fill="#FFFFFF"/><circle cx="387.5" cy="300" r="120" fill="#E30A17"/>'
    tr_star_auth = make_star(490, 300, 50, 20, "#FFFFFF", rot_deg=18)
    tr_star_fake = make_star(380, 300, 50, 20, "#FFFFFF", rot_deg=18)
    tr_svg_auth = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{tr_bg}{tr_star_auth}</svg>'
    tr_svg_fake = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{tr_bg}{tr_star_fake}</svg>'
    save_svg(f"{AUTH_DIR}/flag_tr.svg", tr_svg_auth)
    save_svg(f"{FAKE_DIR}/flag_tr_fake.svg", tr_svg_fake)

    # 18. SA - Saudi Arabia
    # Sword facing left vs facing right
    sa_bg = '<rect width="900" height="600" fill="#007A3D"/><text x="450" y="270" font-family="sans-serif" font-size="70" font-weight="bold" fill="#FFFFFF" text-anchor="middle">لا إله إلا الله محمد رسول الله</text>'
    sa_sword_auth = '<polygon points="200,380 650,380 700,385 650,390 200,390 190,385" fill="#FFFFFF"/><circle cx="190" cy="385" r="15" fill="#007A3D" stroke="#FFFFFF" stroke-width="4"/>'
    sa_sword_fake = '<polygon points="700,380 250,380 200,385 250,390 700,390 710,385" fill="#FFFFFF"/><circle cx="710" cy="385" r="15" fill="#007A3D" stroke="#FFFFFF" stroke-width="4"/>'
    save_svg(f"{AUTH_DIR}/flag_sa.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{sa_bg}{sa_sword_auth}</svg>')
    save_svg(f"{FAKE_DIR}/flag_sa_fake.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{sa_bg}{sa_sword_fake}</svg>')

    # 19. EG - Egypt
    # Eagle facing left vs facing right
    eg_stripes = '<rect width="900" height="200" fill="#CE1126"/><rect y="200" width="900" height="200" fill="#FFFFFF"/><rect y="400" width="900" height="200" fill="#000000"/>'
    eagle_auth = '<g transform="translate(410, 240)"><path d="M 40 10 L 20 40 L 10 90 L 30 110 L 50 110 L 70 90 L 60 40 Z" fill="#C09300"/><rect x="25" y="45" width="30" height="40" fill="#FFFFFF" stroke="#000000" stroke-width="2"/><path d="M 40 10 Q 25 0 10 10 Q 20 20 40 15 Z" fill="#C09300"/></g>'
    eagle_fake = '<g transform="translate(410, 240)"><path d="M 40 10 L 20 40 L 10 90 L 30 110 L 50 110 L 70 90 L 60 40 Z" fill="#C09300"/><rect x="25" y="45" width="30" height="40" fill="#FFFFFF" stroke="#000000" stroke-width="2"/><path d="M 40 10 Q 55 0 70 10 Q 60 20 40 15 Z" fill="#C09300"/></g>'
    save_svg(f"{AUTH_DIR}/flag_eg.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{eg_stripes}{eagle_auth}</svg>')
    save_svg(f"{FAKE_DIR}/flag_eg_fake.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{eg_stripes}{eagle_fake}</svg>')

    # 20. VN - Vietnam
    # 5-pointed vs 6-pointed star
    vn_bg = '<rect width="900" height="600" fill="#DA251D"/>'
    vn_star_auth = make_star(450, 300, 150, 60, "#FFFF00", points=5)
    vn_star_fake = make_star(450, 300, 150, 60, "#FFFF00", points=6)
    save_svg(f"{AUTH_DIR}/flag_vn.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{vn_bg}{vn_star_auth}</svg>')
    save_svg(f"{FAKE_DIR}/flag_vn_fake.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{vn_bg}{vn_star_fake}</svg>')

    # 21. CN - China
    # Arc small stars vs straight line small stars
    cn_bg = '<rect width="900" height="600" fill="#DE2910"/>'
    cn_big = make_star(150, 150, 90, 36, "#FFDE00", points=5)
    cn_sm_auth = "".join([make_star(270, 60, 30, 12, "#FFDE00", rot_deg=-20), make_star(320, 110, 30, 12, "#FFDE00", rot_deg=10), make_star(320, 180, 30, 12, "#FFDE00", rot_deg=40), make_star(270, 230, 30, 12, "#FFDE00", rot_deg=70)])
    cn_sm_fake = "".join([make_star(280, 150, 30, 12, "#FFDE00"), make_star(330, 150, 30, 12, "#FFDE00"), make_star(380, 150, 30, 12, "#FFDE00"), make_star(430, 150, 30, 12, "#FFDE00")])
    save_svg(f"{AUTH_DIR}/flag_cn.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{cn_bg}{cn_big}{cn_sm_auth}</svg>')
    save_svg(f"{FAKE_DIR}/flag_cn_fake.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{cn_bg}{cn_big}{cn_sm_fake}</svg>')

    # 22. AU - Australia
    # Southern Cross with 7-pt stars vs all 5-pt stars
    au_uj = '<rect width="1200" height="600" fill="#00008B"/><rect width="600" height="300" fill="#00247D"/><line x1="0" y1="0" x2="600" y2="300" stroke="#FFFFFF" stroke-width="60"/><line x1="600" y1="0" x2="0" y2="300" stroke="#FFFFFF" stroke-width="60"/><line x1="300" y1="0" x2="300" y2="300" stroke="#FFFFFF" stroke-width="100"/><line x1="0" y1="150" x2="600" y2="150" stroke="#FFFFFF" stroke-width="100"/><line x1="300" y1="0" x2="300" y2="300" stroke="#CC0000" stroke-width="60"/><line x1="0" y1="150" x2="600" y2="150" stroke="#CC0000" stroke-width="60"/>'
    au_cw = make_star(300, 450, 75, 30, "#FFFFFF", points=7)
    au_sc_auth = "".join([make_star(900, 120, 35, 14, "#FFFFFF", points=7), make_star(1050, 270, 35, 14, "#FFFFFF", points=7), make_star(900, 480, 35, 14, "#FFFFFF", points=7), make_star(750, 300, 35, 14, "#FFFFFF", points=7), make_star(960, 360, 20, 8, "#FFFFFF", points=5)])
    au_sc_fake = "".join([make_star(900, 120, 35, 14, "#FFFFFF", points=5), make_star(1050, 270, 35, 14, "#FFFFFF", points=5), make_star(900, 480, 35, 14, "#FFFFFF", points=5), make_star(750, 300, 35, 14, "#FFFFFF", points=5), make_star(960, 360, 20, 8, "#FFFFFF", points=5)])
    save_svg(f"{AUTH_DIR}/flag_au.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 600">{au_uj}{au_cw}{au_sc_auth}</svg>')
    save_svg(f"{FAKE_DIR}/flag_au_fake.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 600">{au_uj}{au_cw}{au_sc_fake}</svg>')

    # 23. NZ - New Zealand
    # 4 stars vs 5 stars
    nz_uj = '<rect width="1200" height="600" fill="#00247D"/><rect width="600" height="300" fill="#00247D"/><line x1="0" y1="0" x2="600" y2="300" stroke="#FFFFFF" stroke-width="60"/><line x1="600" y1="0" x2="0" y2="300" stroke="#FFFFFF" stroke-width="60"/><line x1="300" y1="0" x2="300" y2="300" stroke="#FFFFFF" stroke-width="100"/><line x1="0" y1="150" x2="600" y2="150" stroke="#FFFFFF" stroke-width="100"/><line x1="300" y1="0" x2="300" y2="300" stroke="#CC0000" stroke-width="60"/><line x1="0" y1="150" x2="600" y2="150" stroke="#CC0000" stroke-width="60"/>'
    nz_sc_auth = "".join([make_star(900, 120, 40, 16, "#FFFFFF", points=5) + make_star(900, 120, 30, 12, "#CC0000", points=5),
                          make_star(1030, 240, 35, 14, "#FFFFFF", points=5) + make_star(1030, 240, 25, 10, "#CC0000", points=5),
                          make_star(900, 480, 45, 18, "#FFFFFF", points=5) + make_star(900, 480, 35, 14, "#CC0000", points=5),
                          make_star(770, 300, 30, 12, "#FFFFFF", points=5) + make_star(770, 300, 20, 8, "#CC0000", points=5)])
    nz_sc_fake = nz_sc_auth + make_star(950, 360, 25, 10, "#FFFFFF", points=5) + make_star(950, 360, 18, 7, "#CC0000", points=5)
    save_svg(f"{AUTH_DIR}/flag_nz.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 600">{nz_uj}{nz_sc_auth}</svg>')
    save_svg(f"{FAKE_DIR}/flag_nz_fake.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 600">{nz_uj}{nz_sc_fake}</svg>')

    # 24. RO - Romania
    ro_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="300" height="600" fill="#002B7F"/><rect x="300" width="300" height="600" fill="#FCD116"/><rect x="600" width="300" height="600" fill="#CE1126"/></svg>'
    ro_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="300" height="600" fill="#CE1126"/><rect x="300" width="300" height="600" fill="#FCD116"/><rect x="600" width="300" height="600" fill="#002B7F"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_ro.svg", ro_auth)
    save_svg(f"{FAKE_DIR}/flag_ro_fake.svg", ro_fake)

    # 25. TD - Chad
    td_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="300" height="600" fill="#00205B"/><rect x="300" width="300" height="600" fill="#FFD100"/><rect x="600" width="300" height="600" fill="#C8102E"/></svg>'
    td_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="300" height="600" fill="#00205B"/><rect x="300" width="300" height="600" fill="#FFFFFF"/><rect x="600" width="300" height="600" fill="#C8102E"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_td.svg", td_auth)
    save_svg(f"{FAKE_DIR}/flag_td_fake.svg", td_fake)

    # 26. ID - Indonesia
    id_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="900" height="300" fill="#FF0000"/><rect y="300" width="900" height="300" fill="#FFFFFF"/></svg>'
    id_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="900" height="300" fill="#FFFFFF"/><rect y="300" width="900" height="300" fill="#FF0000"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_id.svg", id_auth)
    save_svg(f"{FAKE_DIR}/flag_id_fake.svg", id_fake)

    # 27. MC - Monaco
    mc_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 800"><rect width="1000" height="400" fill="#CE1126"/><rect y="400" width="1000" height="400" fill="#FFFFFF"/></svg>'
    mc_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 800"><rect width="1000" height="400" fill="#FFD100"/><rect y="400" width="1000" height="400" fill="#FFFFFF"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_mc.svg", mc_auth)
    save_svg(f"{FAKE_DIR}/flag_mc_fake.svg", mc_fake)

    # 28. LB - Lebanon
    # Green Cedar vs Black Cedar
    lb_bg = '<rect width="900" height="150" fill="#ED1C24"/><rect y="150" width="900" height="300" fill="#FFFFFF"/><rect y="450" width="900" height="150" fill="#ED1C24"/>'
    cedar_auth = '<polygon points="450,180 370,300 400,300 350,380 410,380 430,420 470,420 490,380 550,380 500,300 530,300" fill="#00A651"/>'
    cedar_fake = '<polygon points="450,180 370,300 400,300 350,380 410,380 430,420 470,420 490,380 550,380 500,300 530,300" fill="#000000"/>'
    save_svg(f"{AUTH_DIR}/flag_lb.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{lb_bg}{cedar_auth}</svg>')
    save_svg(f"{FAKE_DIR}/flag_lb_fake.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{lb_bg}{cedar_fake}</svg>')

    # 29. GR - Greece
    # 9 stripes vs 8 stripes
    gr_canton = '<rect width="300" height="333.33" fill="#001489"/><rect x="116.67" width="66.67" height="333.33" fill="#FFFFFF"/><rect y="133.33" width="300" height="66.67" fill="#FFFFFF"/>'
    gr_stripes_auth = "".join([f'<rect y="{i*66.67:.2f}" width="900" height="66.67" fill="{"#001489" if i%2==0 else "#FFFFFF"}"/>' for i in range(9)])
    gr_stripes_fake = "".join([f'<rect y="{i*75:.2f}" width="900" height="75" fill="{"#001489" if i%2==0 else "#FFFFFF"}"/>' for i in range(8)])
    save_svg(f"{AUTH_DIR}/flag_gr.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{gr_stripes_auth}{gr_canton}</svg>')
    save_svg(f"{FAKE_DIR}/flag_gr_fake.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">{gr_stripes_fake}{gr_canton}</svg>')

    # 30. CI - Cote d'Ivoire
    ci_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="300" height="600" fill="#FF8200"/><rect x="300" width="300" height="600" fill="#FFFFFF"/><rect x="600" width="300" height="600" fill="#009A44"/></svg>'
    ci_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="300" height="600" fill="#009A44"/><rect x="300" width="300" height="600" fill="#FFFFFF"/><rect x="600" width="300" height="600" fill="#FF8200"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_ci.svg", ci_auth)
    save_svg(f"{FAKE_DIR}/flag_ci_fake.svg", ci_fake)

    # 31. LU - Luxembourg
    lu_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 600"><rect width="1000" height="200" fill="#EA141D"/><rect y="200" width="1000" height="200" fill="#FFFFFF"/><rect y="400" width="1000" height="200" fill="#00A3E0"/></svg>'
    lu_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 600"><rect width="1000" height="200" fill="#EA141D"/><rect y="200" width="1000" height="200" fill="#FFFFFF"/><rect y="400" width="1000" height="200" fill="#21468B"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_lu.svg", lu_auth)
    save_svg(f"{FAKE_DIR}/flag_lu_fake.svg", lu_fake)

    # 32. JM - Jamaica
    jm_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 600"><polygon points="0,0 600,300 0,600" fill="#000000"/><polygon points="1200,0 600,300 1200,600" fill="#000000"/><polygon points="0,0 600,300 1200,0" fill="#007749"/><polygon points="0,600 600,300 1200,600" fill="#007749"/><line x1="0" y1="0" x2="1200" y2="600" stroke="#FFB81C" stroke-width="80"/><line x1="1200" y1="0" x2="0" y2="600" stroke="#FFB81C" stroke-width="80"/></svg>'
    jm_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 600"><polygon points="0,0 600,300 0,600" fill="#007749"/><polygon points="1200,0 600,300 1200,600" fill="#007749"/><polygon points="0,0 600,300 1200,0" fill="#000000"/><polygon points="0,600 600,300 1200,600" fill="#000000"/><line x1="0" y1="0" x2="1200" y2="600" stroke="#FFB81C" stroke-width="80"/><line x1="1200" y1="0" x2="0" y2="600" stroke="#FFB81C" stroke-width="80"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_jm.svg", jm_auth)
    save_svg(f"{FAKE_DIR}/flag_jm_fake.svg", jm_fake)

    # 33. JP - Japan
    jp_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="900" height="600" fill="#FFFFFF"/><circle cx="450" cy="300" r="180" fill="#BC002D"/></svg>'
    jp_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600"><rect width="900" height="600" fill="#FFFFFF"/><circle cx="320" cy="300" r="180" fill="#BC002D"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_jp.svg", jp_auth)
    save_svg(f"{FAKE_DIR}/flag_jp_fake.svg", jp_fake)

    # 34. CH - Switzerland
    ch_auth = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600"><rect width="600" height="600" fill="#FF0000"/><rect x="240" y="100" width="120" height="400" fill="#FFFFFF"/><rect x="100" y="240" width="400" height="120" fill="#FFFFFF"/></svg>'
    ch_fake = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600"><rect width="600" height="600" fill="#FF0000"/><rect x="240" y="0" width="120" height="600" fill="#FFFFFF"/><rect x="0" y="240" width="600" height="120" fill="#FFFFFF"/></svg>'
    save_svg(f"{AUTH_DIR}/flag_ch.svg", ch_auth)
    save_svg(f"{FAKE_DIR}/flag_ch_fake.svg", ch_fake)

    # 35. AR - Argentina
    # Sun with face vs Sun without face
    ar_bg = '<rect width="1400" height="300" fill="#74ACDF"/><rect y="300" width="1400" height="300" fill="#FFFFFF"/><rect y="600" width="1400" height="300" fill="#74ACDF"/>'
    def make_sun(with_face):
        rays = []
        for i in range(32):
            angle = math.radians(i * (360 / 32))
            length = 120 if i % 2 == 0 else 90
            x2 = 700 + length * math.cos(angle)
            y2 = 450 + length * math.sin(angle)
            rays.append(f'<line x1="700" y1="450" x2="{x2:.2f}" y2="{y2:.2f}" stroke="#F6B40E" stroke-width="6"/>')
        face = '<circle cx="680" cy="435" r="5" fill="#000000"/><circle cx="720" cy="435" r="5" fill="#000000"/><path d="M 680 470 Q 700 485 720 470" fill="none" stroke="#000000" stroke-width="4"/>' if with_face else ''
        return f'{"".join(rays)}<circle cx="700" cy="450" r="50" fill="#F6B40E"/>{face}'
    
    save_svg(f"{AUTH_DIR}/flag_ar.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1400 900">{ar_bg}{make_sun(True)}</svg>')
    save_svg(f"{FAKE_DIR}/flag_ar_fake.svg", f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1400 900">{ar_bg}{make_sun(False)}</svg>')

    print("Successfully generated all 35 authentic and fake SVG flags!")

if __name__ == "__main__":
    generate_all()
